import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { dashboardService } from '../services/dashboardService.js';

const NAV_ITEMS = [
  { to: '/', label: 'Aprender', end: true },
  { to: '/practica', label: 'Práctica diaria' },
  { to: '/diccionario', label: 'Diccionario' },
  { to: '/clasificacion', label: 'Clasificación' },
  { to: '/progreso', label: 'Progreso' },
];

function FlameIcon() {
  return (
    <svg className="chip-icon" width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M12 2s4 4 4 8a4 4 0 0 1-8 0c0-1 0-2 1-3 0 2-1 3-1 4 0 3 2 5 4 5s4-2 4-5c0-4-4-9-4-9zm-1 14a2 2 0 1 0 0 4 2 2 0 0 0 0-4z" />
    </svg>
  );
}

function BoltIcon() {
  return (
    <svg className="chip-icon" width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z" />
    </svg>
  );
}

function HamburgerIcon({ open }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      {open ? (
        <path d="M6 6l12 12M6 18L18 6" />
      ) : (
        <>
          <path d="M3 6h18" />
          <path d="M3 12h18" />
          <path d="M3 18h18" />
        </>
      )}
    </svg>
  );
}

function Header() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [badge, setBadge] = useState(null);
  const [avatarOpen, setAvatarOpen] = useState(false);
  const [hamburgerOpen, setHamburgerOpen] = useState(false);
  const menuRef = useRef(null);
  const drawerRef = useRef(null);

  useEffect(() => {
    let cancelled = false;
    dashboardService
      .getMe()
      .then((value) => {
        if (!cancelled) setBadge(value);
      })
      .catch(() => {
        if (!cancelled) setBadge(null);
      });
    return () => { cancelled = true; };
  }, [user]);

  useEffect(() => {
    function handleClick(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setAvatarOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  useEffect(() => {
    function handleDrawerClick(e) {
      if (drawerRef.current && !drawerRef.current.contains(e.target)) {
        setHamburgerOpen(false);
      }
    }
    if (hamburgerOpen) {
      document.addEventListener('mousedown', handleDrawerClick);
    }
    return () => document.removeEventListener('mousedown', handleDrawerClick);
  }, [hamburgerOpen]);

  useEffect(() => {
    if (hamburgerOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => { document.body.style.overflow = ''; };
  }, [hamburgerOpen]);

  const handleLogout = useCallback(() => {
    setAvatarOpen(false);
    setHamburgerOpen(false);
    logout();
    navigate('/login');
  }, [logout, navigate]);

  const closeDrawer = useCallback(() => {
    setHamburgerOpen(false);
  }, []);

  const displayInitials = badge?.initials ?? user?.displayName?.charAt(0)?.toUpperCase() ?? 'U';
  const streakValue = badge?.currentStreak ?? 0;
  const xpValue = badge?.totalXp ?? 0;

  return (
    <header className="header">
      <button
        type="button"
        className="header__hamburger"
        onClick={() => setHamburgerOpen((o) => !o)}
        aria-label={hamburgerOpen ? 'Cerrar menú' : 'Abrir menú'}
      >
        <HamburgerIcon open={hamburgerOpen} />
      </button>

      <Link to="/" className="header__brand" aria-label="enclave inicio">
        <span className="brand-mark">e</span>
        <span className="brand-text">
          <span className="brand-text__title">enclave</span>
          <span className="brand-text__sub">inglés · CEFR</span>
        </span>
      </Link>

      <nav className="header__nav" aria-label="Principal">
        {NAV_ITEMS.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end}>
            {item.label}
          </NavLink>
        ))}
        <NavLink to="/precios" className="header__premium-link">
          Premium
        </NavLink>
      </nav>

      <div className="header__meta">
        {user?.role === 'ADMIN' && (
          <Link to="/admin" className="header__admin-link">
            Admin
          </Link>
        )}
        <span className="header__chip header__chip--desktop" title="Racha actual">
          <FlameIcon />
          <strong>{streakValue}</strong>
          días
        </span>
        <span className="header__chip header__chip--xp header__chip--desktop" title="XP totales">
          <BoltIcon />
          <strong>{xpValue.toLocaleString('es-ES')}</strong>
          XP
        </span>
        {isAuthenticated ? (
          <div className="avatar-dropdown" ref={menuRef}>
            <button
              type="button"
              className="avatar avatar--link"
              onClick={() => setAvatarOpen((o) => !o)}
              title="Menú de usuario"
            >
              {displayInitials}
            </button>
            {avatarOpen && (
              <div className="avatar-dropdown__menu">
                <Link to="/perfil" className="avatar-dropdown__item" onClick={() => setAvatarOpen(false)}>
                  Perfil
                </Link>
                <Link to="/configuracion" className="avatar-dropdown__item" onClick={() => setAvatarOpen(false)}>
                  Configuración
                </Link>
                <Link to="/precios" className="avatar-dropdown__item" onClick={() => setAvatarOpen(false)}>
                  Premium
                </Link>
                <hr className="avatar-dropdown__divider" />
                <button type="button" className="avatar-dropdown__item avatar-dropdown__item--danger" onClick={handleLogout}>
                  Cerrar sesión
                </button>
              </div>
            )}
          </div>
        ) : (
          <Link to="/login" className="avatar avatar--link" title="Iniciar sesión">
            {displayInitials}
          </Link>
        )}
      </div>

      {hamburgerOpen && <div className="drawer-backdrop" onClick={closeDrawer} />}

      <div ref={drawerRef} className={`drawer ${hamburgerOpen ? 'drawer--open' : ''}`}>
        <nav className="drawer__nav" aria-label="Navegación móvil">
          {NAV_ITEMS.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end} className="drawer__link" onClick={closeDrawer}>
              {item.label}
            </NavLink>
          ))}
          <hr className="menu-divider" />
          <div className="drawer__chips">
            <span className="header__chip" title="Racha actual">
              <FlameIcon />
              <strong>{streakValue}</strong>
              {' '}días
            </span>
            <span className="header__chip header__chip--xp" title="XP totales">
              <BoltIcon />
              <strong>{xpValue.toLocaleString('es-ES')}</strong>
              {' '}XP
            </span>
          </div>
          {isAuthenticated && (
            <>
              <hr className="menu-divider" />
              <Link to="/perfil" className="drawer__link" onClick={closeDrawer}>Perfil</Link>
              <Link to="/configuracion" className="drawer__link" onClick={closeDrawer}>Configuración</Link>
              <Link to="/precios" className="drawer__link drawer__link--premium" onClick={closeDrawer}>Premium</Link>
              <button type="button" className="drawer__link drawer__link--danger" onClick={handleLogout}>
                Cerrar sesión
              </button>
            </>
          )}
          {user?.role === 'ADMIN' && (
            <>
              <hr className="menu-divider" />
              <Link to="/admin" className="drawer__link" onClick={closeDrawer}>Admin</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

export default Header;
