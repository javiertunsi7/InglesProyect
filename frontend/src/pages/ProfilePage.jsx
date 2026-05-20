import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { userService } from '../services/userService.js';

function BoltIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z" />
    </svg>
  );
}

function FlameIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M12 2s4 4 4 8a4 4 0 0 1-8 0c0-1 0-2 1-3 0 2-1 3-1 4 0 3 2 5 4 5s4-2 4-5c0-4-4-9-4-9zm-1 14a2 2 0 1 0 0 4 2 2 0 0 0 0-4z" />
    </svg>
  );
}

function SettingsIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
    </svg>
  );
}

function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    userService
      .getProfile()
      .then((data) => {
        if (!cancelled) setProfile(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return <div className="state"><p>Cargando perfil...</p></div>;
  }

  if (error) {
    return <div className="state state--error"><p>{error}</p></div>;
  }

  if (!profile) {
    return <div className="state state--error"><p>No se pudo cargar el perfil.</p></div>;
  }

  return (
    <div className="page page--profile">
      <div className="profile-header">
        <div className="profile-avatar">
          {profile.avatarUrl ? (
            <img src={profile.avatarUrl} alt={profile.displayName} className="profile-avatar__img" />
          ) : (
            <span className="profile-avatar__initials">{profile.initials}</span>
          )}
        </div>
        <div className="profile-header__info">
          <h1 className="profile-header__name">{profile.displayName}</h1>
          <p className="profile-header__email">{profile.email}</p>
          {profile.bio && <p className="profile-header__bio">{profile.bio}</p>}
        </div>
        <Link to="/configuracion" className="btn btn--ghost profile-header__settings">
          <SettingsIcon /> Ajustes
        </Link>
      </div>

      <div className="profile-stats">
        <div className="stat-card">
          <FlameIcon />
          <span className="stat-card__value">{profile.currentStreak}</span>
          <span className="stat-card__label">Racha actual</span>
        </div>
        <div className="stat-card">
          <BoltIcon />
          <span className="stat-card__value">{profile.totalXp.toLocaleString('es-ES')}</span>
          <span className="stat-card__label">XP totales</span>
        </div>
        <div className="stat-card">
          <span className="stat-card__value">{profile.dailyGoalMinutes}</span>
          <span className="stat-card__label">Meta diaria (min)</span>
        </div>
        <div className="stat-card">
          <span className="stat-card__value">{profile.dailyGoalXp}</span>
          <span className="stat-card__label">Meta diaria (XP)</span>
        </div>
      </div>
    </div>
  );
}

export default ProfilePage;
