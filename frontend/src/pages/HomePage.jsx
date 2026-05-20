import { Link, useNavigate } from 'react-router-dom';
import { useFetch } from '../hooks/useFetch.js';
import { useTTS } from '../hooks/useTTS.js';
import { dashboardService } from '../services/dashboardService.js';
import DailyQuests from '../components/DailyQuests.jsx';

function GlobeIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <circle cx="12" cy="12" r="9" />
      <path d="M3 12h18M12 3a13 13 0 0 1 0 18M12 3a13 13 0 0 0 0 18" />
    </svg>
  );
}

function TerminalIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <rect x="3" y="4" width="18" height="16" rx="2" />
      <path d="m7 9 3 3-3 3M13 15h4" />
    </svg>
  );
}

function PlayIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M8 5v14l11-7z" />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M5 12h14M13 6l6 6-6 6" />
    </svg>
  );
}

function SpeakerIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M11 5 6 9H2v6h4l5 4V5zM15.5 8.5a5 5 0 0 1 0 7M18 6a8 8 0 0 1 0 12" />
    </svg>
  );
}

const TRACK_ICONS = {
  GENERAL: <GlobeIcon />,
  TECH: <TerminalIcon />,
};

function formatTime() {
  const date = new Date();
  return date.toLocaleDateString('es-ES', { weekday: 'long' });
}

function HomePage() {
  const { speak } = useTTS();
  const { data, loading, error } = useFetch(() => dashboardService.getDashboard(), []);
  const navigate = useNavigate();

  if (loading) return <p className="state">Cargando...</p>;
  if (error) return <p className="state state--error">{error}</p>;
  if (!data) return null;

  const { greeting, continueCard, tracks, streak, dailyGoal, wordOfDay } = data;

  return (
    <section className="page page--split dashboard">
      <div className="dashboard__main">
        <div className="dashboard__hero">
          <p className="eyebrow">
            {greeting?.salutation ?? 'Hola'}, {greeting?.displayName ?? 'invitado'}
          </p>
          <h1 className="display">{greeting?.headline ?? 'Hoy practicas conversación.'}</h1>
          <p className="lead" style={{ marginTop: '1rem' }}>
            {greeting?.subhead ??
              'Continúa donde lo dejaste, o explora una pista nueva. Cada sesión te toma 8 minutos.'}
          </p>

          {continueCard && (
            <article className="continue-card" style={{ marginTop: '2rem' }}>
              <div className="continue-card__icon">
                <PlayIcon />
              </div>
              <div>
                <p className="continue-card__label">Continuar</p>
                <h2 className="continue-card__title">
                  {continueCard.categoryName} · {continueCard.levelName} · Ejercicio{' '}
                  {continueCard.exercisePosition}
                </h2>
                <p className="continue-card__meta">
                  {continueCard.topic} · {continueCard.questionsRemaining} preguntas restantes · ~
                  {continueCard.estimatedMinutes} min
                </p>
                {Number(continueCard.dueToday ?? 0) > 0 && (
                  <div className="continue-card__due">
                    <span className="due-chip">
                      {continueCard.dueToday}{' '}
                      {continueCard.dueToday === 1 ? 'palabra' : 'palabras'} para repasar hoy
                    </span>
                    <Link to="/practica" className="btn btn--ghost btn--tiny">
                      Ir a práctica
                    </Link>
                  </div>
                )}
              </div>
              <button
                type="button"
                className="btn btn--primary"
                onClick={() =>
                  navigate(
                    `/${continueCard.categoryType}/${continueCard.levelCode}/${continueCard.exerciseId}`,
                  )
                }
              >
                Reanudar
                <ArrowIcon />
              </button>
            </article>
          )}
        </div>

        <section style={{ marginTop: '3rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <h2 className="section-title">Elige una pista</h2>
            <span className="eyebrow eyebrow--muted">{tracks.length} disponibles</span>
          </div>
          <div className="track-grid" style={{ marginTop: '1.25rem' }}>
            {tracks.map((track) => (
              <Link key={track.id} to={`/${track.type}`} className="track-card">
                <div className="track-card__icon">{TRACK_ICONS[track.type] ?? <GlobeIcon />}</div>
                <div>
                  <h3 className="track-card__title">{track.displayName}</h3>
                  <p className="track-card__desc">{track.description}</p>
                </div>
                <div className="track-card__stats">
                  <div>
                    <div className="track-card__stat">
                      <span className="track-card__stat-value">{track.totalLevels}</span>
                      <span className="track-card__stat-label">Niveles</span>
                    </div>
                  </div>
                  <div className="track-card__stat" style={{ marginRight: 'auto', marginLeft: '1.5rem' }}>
                    <span className="track-card__stat-value">{track.progressPercent}%</span>
                    <span className="track-card__stat-label">Avance</span>
                  </div>
                  <span className="track-card__arrow"><ArrowIcon /></span>
                </div>
              </Link>
            ))}
          </div>
        </section>
      </div>

      <aside className="sidebar">
        {streak && (
          <article className="card">
            <h3 className="card__title">Racha</h3>
            <p className="card__sub">Mantén la llama encendida cada día.</p>
            <div className="streak-card__days">
              {streak.week.map((day, idx) => (
                <span
                  key={idx}
                  className={`streak-day${day.active ? ' streak-day--active' : ''}${
                    day.today ? ' streak-day--today' : ''
                  }`}
                >
                  {day.dayLabel}
                </span>
              ))}
            </div>
            <div className="streak-card__total">
              <span>
                <span className="streak-card__total-number">{streak.currentStreak}</span>
                <span className="muted">días seguidos</span>
              </span>
              <span title="Racha más larga" aria-label="racha máxima">
                <span className="muted">máx · </span>
                <strong>{streak.longestStreak}</strong>
              </span>
            </div>
          </article>
        )}

        {dailyGoal && (
          <article className="card">
            <h3 className="card__title">Meta diaria</h3>
            <p className="card__sub">
              {dailyGoal.minutesPracticed} de {dailyGoal.dailyGoalMinutes} minutos hoy
            </p>
            <div className="goal-card__progress">
              <div
                className="goal-card__progress-fill"
                style={{ width: `${dailyGoal.progressPercent}%` }}
              />
            </div>
            <div className="goal-card__footer">
              <span>{dailyGoal.minutesRemaining} min restantes</span>
              <span>
                {dailyGoal.xpEarned} / {dailyGoal.dailyGoalXp} XP
              </span>
            </div>
          </article>
        )}

        <DailyQuests />

        {wordOfDay && (
          <article className="card">
            <p className="eyebrow eyebrow--muted">Palabra del día</p>
            <h3 className="word-card__word">{wordOfDay.word}</h3>
            <div>
              <span className="word-card__phonetic">{wordOfDay.phonetic}</span>
              <span className="word-card__pos">· {wordOfDay.partOfSpeech}</span>
            </div>
            <p className="word-card__definition">{wordOfDay.definitionEs}</p>
            <div className="word-card__actions">
              <button type="button" className="btn btn--ghost" onClick={() => speak(wordOfDay.word)}>
                <SpeakerIcon /> Escuchar
              </button>
              <button type="button" className="btn btn--quiet">
                Guardar
              </button>
            </div>
          </article>
        )}

        <p className="muted" style={{ fontSize: '0.75rem', textAlign: 'center' }}>
          Hoy es {formatTime()}.
        </p>
      </aside>
    </section>
  );
}

export default HomePage;
