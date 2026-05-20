import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import QuestionCard from '../components/QuestionCard.jsx';
import { practiceService } from '../services/practiceService.js';

function PlusIcon() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M12 4v16M4 12h16" stroke="currentColor" strokeWidth="2" fill="none" />
    </svg>
  );
}

function ClockIcon() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </svg>
  );
}

function BoltIcon() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z" />
    </svg>
  );
}

function PracticePage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [stats, setStats] = useState({ ok: 0, err: 0, done: 0 });

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    practiceService
      .getDaily()
      .then((value) => !cancelled && setData(value))
      .catch((err) => !cancelled && setError(err.message))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, []);

  const totalQuestions = data?.questions?.length ?? 0;
  const progressPct = useMemo(
    () => (totalQuestions === 0 ? 0 : (stats.done / totalQuestions) * 100),
    [stats.done, totalQuestions],
  );

  const handleAnswered = (result) => {
    setStats((prev) => ({
      ok: prev.ok + (result.correct ? 1 : 0),
      err: prev.err + (result.correct ? 0 : 1),
      done: prev.done + 1,
    }));
  };

  if (loading) return <p className="state">Preparando tu sesión...</p>;
  if (error) return <p className="state state--error">{error}</p>;
  if (!data) return null;

  const finished = stats.done === totalQuestions && totalQuestions > 0;
  const earnedXp = stats.ok * Math.round((data.expectedXp ?? 0) / Math.max(totalQuestions, 1));
  // forecast[0] = hoy, forecast[1] = mañana. Backend devuelve los próximos 7 días.
  const dueCount = data.dueCount ?? 0;
  const newCount = data.newCount ?? 0;
  const tomorrowDue = Array.isArray(data.forecast) && data.forecast.length > 1
    ? Number(data.forecast[1].count ?? 0)
    : 0;

  return (
    <section className="page">
      <div>
        <Link to="/" className="back-link">
          ← Volver al inicio
        </Link>
        <p className="eyebrow">Práctica diaria · {new Date(data.date ?? Date.now()).toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' })}</p>
        <h1 className="display">{data.headline}</h1>
        {data.subhead && <p className="lead" style={{ marginTop: '1rem' }}>{data.subhead}</p>}

        <div className="exercise-meta">
          {dueCount > 0 && (
            <span className="due-chip" title="Preguntas que tocaba repasar hoy">
              <ClockIcon /> {dueCount} {dueCount === 1 ? 'repaso' : 'repasos'}
            </span>
          )}
          {newCount > 0 && (
            <span className="new-chip" title="Vocabulario nuevo de hoy">
              <PlusIcon /> {newCount} {newCount === 1 ? 'nueva' : 'nuevas'}
            </span>
          )}
          {dueCount === 0 && newCount === 0 && (
            <span>
              <PlusIcon /> {totalQuestions} preguntas
            </span>
          )}
          <span>
            <ClockIcon /> ~{data.targetMinutes ?? 10} min
          </span>
          <span>
            <BoltIcon /> hasta {data.expectedXp ?? 0} XP
          </span>
        </div>

        {totalQuestions > 0 && (
          <div className="exercise-progress-bar">
            <span className="exercise-progress-bar__label">
              <strong>{stats.done}</strong>/{totalQuestions}
            </span>
            <div className="exercise-progress-bar__track">
              <div className="exercise-progress-bar__fill" style={{ width: `${progressPct}%` }} />
            </div>
            <span className="exercise-progress-bar__label">
              {stats.ok} OK · {stats.err} ERR
            </span>
          </div>
        )}

        {finished && (
          <div className="completion-banner" role="status">
            <div className="completion-banner__title">¡Sesión completada!</div>
            <div className="completion-banner__meta">
              <span>
                <BoltIcon /> +{earnedXp} XP
              </span>
              <span>
                {stats.ok} de {totalQuestions} acertadas
              </span>
            </div>
            {tomorrowDue > 0 && (
              <p className="completion-banner__forecast">
                Mañana toca repasar {tomorrowDue}{' '}
                {tomorrowDue === 1 ? 'palabra' : 'palabras'}.
              </p>
            )}
            <Link to="/" className="btn btn--ghost">
              Volver al inicio
            </Link>
          </div>
        )}

        {totalQuestions === 0 ? (
          <p className="state" style={{ marginTop: '2rem' }}>
            Aún no hay preguntas en tu sesión. Vuelve cuando hayas completado algún ejercicio.
          </p>
        ) : (
          <div className="question-list">
            {data.questions.map((question, idx) => (
              <QuestionCard
                key={question.id}
                question={question}
                index={idx + 1}
                onAnswered={handleAnswered}
                disabled={finished}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export default PracticePage;
