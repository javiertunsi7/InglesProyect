import { useState } from 'react';
import { useSearchParams, Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService.js';

function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token') || '';

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (newPassword.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setError('Las contraseñas no coinciden.');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await authService.resetPassword(token, newPassword);
      setDone(true);
    } catch (err) {
      setError(err?.response?.data?.message || err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (!token) {
    return (
      <section className="page page--auth">
        <article className="auth-card" style={{ textAlign: 'center' }}>
          <h1 className="section-title">Enlace inválido</h1>
          <p className="muted">Este enlace de recuperación no es válido o ha expirado.</p>
          <Link to="/recuperar" className="btn btn--ghost" style={{ marginTop: '1.25rem', display: 'inline-block' }}>
            Solicitar nuevo enlace
          </Link>
        </article>
      </section>
    );
  }

  if (done) {
    return (
      <section className="page page--auth">
        <article className="auth-card" style={{ textAlign: 'center' }}>
          <h1 className="section-title">Contraseña actualizada</h1>
          <p className="muted">Tu contraseña se ha cambiado correctamente.</p>
          <Link to="/login" className="btn btn--primary" style={{ marginTop: '1.25rem', display: 'inline-block' }}>
            Iniciar sesión
          </Link>
        </article>
      </section>
    );
  }

  return (
    <section className="page page--auth">
      <article className="auth-card">
        <p className="eyebrow">Nueva contraseña</p>
        <h1 className="section-title">Crea una contraseña nueva</h1>

        <form onSubmit={handleSubmit} className="auth-form">
          <label className="auth-form__field">
            <span>Nueva contraseña</span>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={6}
              autoComplete="new-password"
            />
          </label>
          <label className="auth-form__field">
            <span>Confirmar contraseña</span>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={6}
              autoComplete="new-password"
            />
          </label>

          {error && <p className="alert alert--error">{error}</p>}

          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? 'Cambiando...' : 'Cambiar contraseña'}
          </button>
        </form>
      </article>
    </section>
  );
}

export default ResetPasswordPage;
