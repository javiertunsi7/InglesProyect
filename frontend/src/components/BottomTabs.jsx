import { NavLink } from 'react-router-dom';

function HomeIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden>
      <path d="M3 12L12 3l9 9" />
      <path d="M5 10v9a1 1 0 0 0 1 1h3v-5h6v5h3a1 1 0 0 0 1-1v-9" />
    </svg>
  );
}

function PracticeIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden>
      <path d="M12 2v4M12 18v4M4 12H2M22 12h-2M19.07 4.93l-2.83 2.83M7.76 16.24l-2.83 2.83M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83" />
      <circle cx="12" cy="12" r="4" />
    </svg>
  );
}

function DictionaryIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden>
      <path d="M2 4h16a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4" />
      <path d="M6 2v5l3-2 3 2V2" />
    </svg>
  );
}

function LeaderboardIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden>
      <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9" />
      <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9" />
      <path d="M4 22h16" />
      <path d="M10 22V8h4v14" />
      <path d="M6 22v-6h4" />
      <path d="M14 22v-6h4" />
    </svg>
  );
}

function ProgressIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden>
      <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
    </svg>
  );
}

const TABS = [
  { to: '/', icon: HomeIcon, label: 'Aprender', end: true },
  { to: '/practica', icon: PracticeIcon, label: 'Practicar' },
  { to: '/diccionario', icon: DictionaryIcon, label: 'Diccionario' },
  { to: '/clasificacion', icon: LeaderboardIcon, label: 'Clasif.' },
  { to: '/progreso', icon: ProgressIcon, label: 'Progreso' },
];

function BottomTabs() {
  return (
    <nav className="bottom-tabs" aria-label="Navegación principal">
      {TABS.map(({ to, icon: Icon, label, end }) => (
        <NavLink key={to} to={to} end={end} className="bottom-tabs__link">
          <Icon />
          <span className="bottom-tabs__label">{label}</span>
        </NavLink>
      ))}
    </nav>
  );
}

export default BottomTabs;
