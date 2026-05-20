import { Link } from 'react-router-dom';
import { useFetch } from '../hooks/useFetch.js';
import { progressService } from '../services/progressService.js';
import WeeklyBars from '../components/WeeklyBars.jsx';
import ForecastBars from '../components/ForecastBars.jsx';
import StatsSection from '../components/StatsSection.jsx';

const CATEGORY_LABEL = {
  GENERAL: 'Inglés General',
  TECH: 'Inglés Técnico',
};

function FlameIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M12 2s4 4 4 8a4 4 0 0 1-8 0c0-1 0-2 1-3 0 2-1 3-1 4 0 3 2 5 4 5s4-2 4-5c0-4-4-9-4-9z" />
    </svg>
  );
}

function BoltIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z" />
    </svg>
  );
}

function ClockIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </svg>
  );
}

function StarIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="m12 17 6 4-2-7 5-5h-7L12 2 9 9H2l5 5-2 7z" />
    </svg>
  );
}

function StatTile({ icon, value, label }) {
  return (
    <div className="stat-tile">
      <span className="stat-tile__icon">{icon}</span>
      <span className="stat-tile__value">{value}</span>
      <span className="stat-tile__label">{label}</span>
    </div>
  );
}

function ProgressPage() {
  const { data, loading, error } = useFetch(() => progressService.getOverview(), []);

  if (loading) return <p className="state">Cargando tu progreso...</p>;
  if (error) return <p className="state state--error">{error}</p>;
  if (!data) return null;

  return (
    <section className="page">
      <div>
        <Link to="/" className="back-link">
          ← Volver al inicio
        </Link>
        <p className="eyebrow">Tu progreso · resumen global</p>
        <h1 className="display">Lo que has aprendido.</h1>
        <p className="lead" style={{ marginTop: '1rem' }}>
          Métricas combinadas de Inglés General y Técnico. Cada acierto suma XP y mantiene tu racha.
        </p>

        <div className="stat-grid" style={{ marginTop: '2rem' }}>
          <StatTile icon={<BoltIcon />} value={(data.totalXp ?? 0).toLocaleString('es-ES')} label="XP totales" />
          <StatTile icon={<FlameIcon />} value={data.currentStreak ?? 0} label="Días de racha" />
          <StatTile icon={<StarIcon />} value={data.totalStars ?? 0} label="Estrellas" />
          <StatTile icon={<ClockIcon />} value={`${data.lifetimeMinutes ?? 0} min`} label="Tiempo total" />
          <StatTile icon="✓" value={data.totalCompletedExercises ?? 0} label="Ejercicios" />
          <StatTile icon="⏤" value={data.longestStreak ?? 0} label="Racha más larga" />
        </div>

        <h2 className="section-title" style={{ marginTop: '3rem' }}>Por pista</h2>
        <div className="track-progress-list">
          {data.tracks.map((track) => (
            <article key={track.categoryType} className="track-progress">
              <header className="track-progress__head">
                <div>
                  <h3 className="track-progress__title">
                    {CATEGORY_LABEL[track.categoryType] ?? track.displayName}
                  </h3>
                  <span className="muted" style={{ fontSize: '0.85rem' }}>
                    {track.completedExercises} de {track.totalExercises} ejercicios · {track.xpEarned} XP
                  </span>
                </div>
                <span className="track-progress__percent">{track.progressPercent}%</span>
              </header>
              <div className="progress-bar">
                <div className="progress-bar__fill" style={{ width: `${track.progressPercent}%` }} />
              </div>
              <div className="track-progress__levels">
                {track.levels.map((level) => (
                  <Link
                    key={level.id}
                    to={`/${track.categoryType}/${level.code}`}
                    className={`mini-level${level.locked ? ' mini-level--locked' : ''}`}
                  >
                    <span className="mini-level__code">{level.code}</span>
                    <span className="mini-level__name">{level.displayName}</span>
                    <span className="mini-level__progress">
                      {level.completedExercises}/{level.totalExercises}
                    </span>
                  </Link>
                ))}
              </div>
            </article>
          ))}
        </div>

        <h2 className="section-title" style={{ marginTop: '3rem' }}>Últimas dos semanas</h2>
        <div style={{ background: 'rgba(255,255,255,0.025)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 14, padding: '1.25rem', marginTop: '0.75rem' }}>
          <WeeklyBars days={data.history} ariaLabel="Actividad últimas dos semanas" />
        </div>

        <h2 className="section-title" style={{ marginTop: '3rem' }}>Próximos repasos</h2>
        <p className="muted" style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
          El SRS programa cuándo vuelve a aparecer cada palabra. Esto es lo que te
          tocará repasar en los próximos 14 días.
        </p>
        <div style={{ background: 'rgba(255,255,255,0.025)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 14, padding: '1.25rem', marginTop: '0.75rem' }}>
          <ForecastBars forecast={data.srsForecast ?? []} />
        </div>

        <h2 className="section-title" style={{ marginTop: '3rem' }}>Logros</h2>
        <div className="achievement-grid">
          {data.achievements.map((a) => (
            <article
              key={a.code}
              className={`achievement${a.unlocked ? ' achievement--unlocked' : ''}`}
            >
              <span className="achievement__icon" aria-hidden>{a.icon}</span>
              <div>
                <h4 className="achievement__title">{a.title}</h4>
                <p className="achievement__desc">{a.description}</p>
                <div className="achievement__bar">
                  <div
                    className="achievement__bar-fill"
                    style={{ width: `${Math.min(100, ((a.progress ?? 0) / (a.target || 1)) * 100)}%` }}
                  />
                </div>
                <span className="muted" style={{ fontSize: '0.75rem' }}>
                  {a.progress} / {a.target}
                </span>
              </div>
            </article>
          ))}
        </div>

        <StatsSection />
      </div>
    </section>
  );
}

export default ProgressPage;
