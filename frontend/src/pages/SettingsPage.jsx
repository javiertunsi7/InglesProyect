import { useEffect, useState } from 'react';
import { userService } from '../services/userService.js';
import { getSubscriptionStatus, createPortalSession } from '../services/subscriptionService.js';
import { usePushNotifications } from '../hooks/usePushNotifications.js';

function SettingsPage() {

  const [displayName, setDisplayName] = useState('');
  const [bio, setBio] = useState('');
  const [dailyGoalMinutes, setDailyGoalMinutes] = useState(15);
  const [dailyGoalXp, setDailyGoalXp] = useState(50);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const [subscription, setSubscription] = useState(null);
  const [subLoading, setSubLoading] = useState(true);
  const [portalLoading, setPortalLoading] = useState(false);
  const [portalError, setPortalError] = useState(null);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [changingPwd, setChangingPwd] = useState(false);
  const [pwdMessage, setPwdMessage] = useState(null);
  const [pwdError, setPwdError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    userService
      .getProfile()
      .then((data) => {
        if (!cancelled) {
          setDisplayName(data.displayName || '');
          setBio(data.bio || '');
          setDailyGoalMinutes(data.dailyGoalMinutes ?? 15);
          setDailyGoalXp(data.dailyGoalXp ?? 50);
        }
      })
      .catch(() => {})
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    getSubscriptionStatus()
      .then((data) => {
        if (!cancelled) setSubscription(data);
      })
      .catch(() => {})
      .finally(() => {
        if (!cancelled) setSubLoading(false);
      });

    return () => { cancelled = true; };
  }, []);

  const handlePortal = async () => {
    setPortalLoading(true);
    setPortalError(null);
    try {
      const { url } = await createPortalSession();
      window.location.href = url;
    } catch (err) {
      setPortalError('Error al abrir el portal de pago.');
    } finally {
      setPortalLoading(false);
    }
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    setError(null);
    try {
      const updated = await userService.updateProfile({
        displayName: displayName.trim(),
        bio: bio.trim(),
        dailyGoalMinutes,
        dailyGoalXp,
      });
      setDisplayName(updated.displayName);
      setBio(updated.bio || '');
      setMessage('Perfil actualizado correctamente.');
    } catch (err) {
      setError(err?.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setPwdError('Las contraseñas no coinciden.');
      return;
    }
    if (newPassword.length < 6) {
      setPwdError('La contraseña debe tener al menos 6 caracteres.');
      return;
    }
    setChangingPwd(true);
    setPwdMessage(null);
    setPwdError(null);
    try {
      await userService.changePassword(currentPassword, newPassword);
      setPwdMessage('Contraseña actualizada correctamente.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setPwdError(err?.response?.data?.message || err.message);
    } finally {
      setChangingPwd(false);
    }
  };

  const { isSupported, permission, isSubscribed, loading: pushLoading, error: pushError, subscribe, unsubscribe, sendTest } = usePushNotifications();

  if (loading) {
    return <div className="state"><p>Cargando...</p></div>;
  }

  return (
    <div className="page page--settings">
      <h1 className="page__title">Configuración</h1>

      <form className="settings-form" onSubmit={handleSaveProfile}>
        <h2 className="settings-form__title">Perfil</h2>

        <label className="settings-form__field">
          <span>Nombre</span>
          <input
            type="text"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            className="question-card__input"
          />
        </label>

        <label className="settings-form__field">
          <span>Biografía</span>
          <textarea
            value={bio}
            onChange={(e) => setBio(e.target.value)}
            className="question-card__input"
            rows={3}
            placeholder="Cuéntanos algo sobre ti..."
          />
        </label>

        <label className="settings-form__field">
          <span>Meta diaria (minutos)</span>
          <input
            type="number"
            min={5}
            max={120}
            value={dailyGoalMinutes}
            onChange={(e) => setDailyGoalMinutes(Number(e.target.value))}
            className="question-card__input"
          />
        </label>

        <label className="settings-form__field">
          <span>Meta diaria (XP)</span>
          <input
            type="number"
            min={10}
            max={1000}
            value={dailyGoalXp}
            onChange={(e) => setDailyGoalXp(Number(e.target.value))}
            className="question-card__input"
          />
        </label>

        <button type="submit" className="btn btn--primary" disabled={saving}>
          {saving ? 'Guardando...' : 'Guardar cambios'}
        </button>

        {message && <p className="feedback feedback--ok">{message}</p>}
        {error && <p className="feedback feedback--ko">{error}</p>}
      </form>

      <div className="settings-form">
        <h2 className="settings-form__title">Suscripción</h2>
        {subLoading ? (
          <p className="settings-form__note">Cargando estado de suscripción...</p>
        ) : subscription?.premium ? (
          <div className="sub-section">
            <div className="sub-section__row">
              <span className="sub-section__label">Plan</span>
              <span className="sub-section__value">{subscription.plan === 'yearly' ? 'Anual' : 'Mensual'}</span>
            </div>
            <div className="sub-section__row">
              <span className="sub-section__label">Estado</span>
              <span className="sub-section__value sub-section__value--active">
                {subscription.status === 'trialing' ? 'Periodo de prueba' : 'Activa'}
              </span>
            </div>
            {subscription.currentPeriodEnd && (
              <div className="sub-section__row">
                <span className="sub-section__label">
                  {subscription.cancelAtPeriodEnd ? 'Finaliza el' : 'Renueva el'}
                </span>
                <span className="sub-section__value">
                  {new Date(subscription.currentPeriodEnd).toLocaleDateString('es-ES')}
                </span>
              </div>
            )}
            {subscription.trialEnd && subscription.status === 'trialing' && (
              <div className="sub-section__row">
                <span className="sub-section__label">Fin de prueba</span>
                <span className="sub-section__value">
                  {new Date(subscription.trialEnd).toLocaleDateString('es-ES')}
                </span>
              </div>
            )}
            <button
              type="button"
              className="btn btn--secondary"
              onClick={handlePortal}
              disabled={portalLoading}
            >
              {portalLoading ? 'Abriendo portal...' : 'Gestionar suscripción'}
            </button>
            {portalError && <p className="feedback feedback--ko">{portalError}</p>}
          </div>
        ) : (
          <div className="sub-section">
            <p className="settings-form__note">
              Sin suscripción activa. Los niveles C1 y C2 requieren Premium.
            </p>
            <a href="/precios" className="btn btn--primary">
              Ver planes Premium
            </a>
          </div>
        )}
      </div>

      {isSupported && (
        <div className="settings-form">
          <h2 className="settings-form__title">Notificaciones</h2>

          <div className="notif-section">
            <div className="notif-section__row">
              <span className="notif-section__label">Notificaciones push</span>
              {pushLoading ? (
                <span className="notif-section__status">Cargando...</span>
              ) : (
                <button
                  type="button"
                  className={`btn ${isSubscribed ? 'btn--danger' : 'btn--primary'}`}
                  onClick={isSubscribed ? unsubscribe : subscribe}
                >
                  {isSubscribed ? 'Desactivar' : 'Activar'}
                </button>
              )}
            </div>

            {permission === 'denied' && (
              <p className="feedback feedback--ko">
                Notificaciones bloqueadas. Actívalas desde la configuración del navegador.
              </p>
            )}

            {pushError && <p className="feedback feedback--ko">{pushError}</p>}

            {isSubscribed && (
              <button type="button" className="btn btn--secondary" onClick={sendTest}>
                Enviar notificación de prueba
              </button>
            )}
          </div>
        </div>
      )}

      {!isSupported && (
        <div className="settings-form">
          <h2 className="settings-form__title">Notificaciones</h2>
          <p className="settings-form__note">Las notificaciones push no están disponibles en este navegador.</p>
        </div>
      )}

      <form className="settings-form" onSubmit={handleChangePassword}>
        <h2 className="settings-form__title">Cambiar contraseña</h2>

        <label className="settings-form__field">
          <span>Contraseña actual</span>
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="question-card__input"
            autoComplete="current-password"
          />
        </label>

        <label className="settings-form__field">
          <span>Nueva contraseña</span>
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            className="question-card__input"
            autoComplete="new-password"
          />
        </label>

        <label className="settings-form__field">
          <span>Confirmar contraseña</span>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            className="question-card__input"
            autoComplete="new-password"
          />
        </label>

        <button type="submit" className="btn btn--primary" disabled={changingPwd || !currentPassword || !newPassword || !confirmPassword}>
          {changingPwd ? 'Cambiando...' : 'Cambiar contraseña'}
        </button>

        {pwdMessage && <p className="feedback feedback--ok">{pwdMessage}</p>}
        {pwdError && <p className="feedback feedback--ko">{pwdError}</p>}
      </form>
    </div>
  );
}

export default SettingsPage;
