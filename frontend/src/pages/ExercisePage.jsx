import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import QuestionCard from '../components/QuestionCard.jsx';
import { exerciseService } from '../services/exerciseService.js';

function BackIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M19 12H5M11 6l-6 6 6 6" />
    </svg>
  );
}

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

function ExercisePage() {
  const { categoryType, levelCode, exerciseId } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // ok/total vienen del backend (fuente de verdad), err es local (done - ok)
  const [stats, setStats] = useState({ ok: 0, err: 0, done: 0 });
  const [completion, setCompletion] = useState(null); // {xpEarned, stars} cuando termina

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    exerciseService
      .getDetail(exerciseId)
      .then((value) => {
        if (cancelled) return;
        setData(value);
        const ok = value.correctAnswers ?? 0;
        const done = value.totalAnswered ?? 0;
        setStats({ ok, err: Math.max(done - ok, 0), done });
        if (value.status === 'COMPLETED') {
          setCompletion({ xpEarned: value.xpReward, stars: value.stars });
        }
      })
      .catch((err) => !cancelled && setError(err.message))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [exerciseId]);

  const handleAnswered = (result) => {
    // Si el backend nos devuelve contadores de progreso del ejercicio los
    // usamos como fuente de verdad; si no (visitante anónimo), incrementamos
    // localmente.
    setStats((prev) => {
      const hasServerCounts = result.totalAnswered !== undefined && result.totalAnswered !== null
        && result.correctAnswers !== undefined && result.correctAnswers !== null;
      if (hasServerCounts) {
        const ok = result.correctAnswers;
        const done = result.totalAnswered;
        return { ok, err: Math.max(done - ok, 0), done };
      }
      return {
        ok: prev.ok + (result.correct ? 1 : 0),
        err: prev.err + (result.correct ? 0 : 1),
        done: prev.done + 1,
      };
    });
    if (result.exerciseCompleted) {
      setCompletion({
        xpEarned: result.xpEarned ?? 0,
        stars: result.stars ?? 0,
      });
    }
  };

  if (loading) return <p className="state">Cargando ejercicio...</p>;
  if (error) return <p className="state state--error">{error}</p>;
  if (!data) return null;

  const totalQuestions = data.questions.length;
  const progressPct = totalQuestions === 0 ? 0 : (stats.done / totalQuestions) * 100;
  const completed = Boolean(completion);

  return (
    <section className="page">
      <div>
        <Link to={`/${categoryType}/${levelCode}`} className="back-link">
          <BackIcon /> Volver a la lista
        </Link>
        <p className="eyebrow">
          {data.categoryType === 'TECH' ? 'Inglés Técnico' : 'Inglés General'} · Nivel {data.levelCode}{' '}
          · {data.topic ?? data.title}
        </p>
        <h1 className="display">Ejercicio {data.position}.</h1>
        <div className="exercise-meta">
          <span>
            <PlusIcon /> {totalQuestions} preguntas
          </span>
          <span>
            <ClockIcon /> ~{data.estimatedMinutes} min
          </span>
          <span>
            <BoltIcon /> {data.xpReward} XP
          </span>
        </div>

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

        {completed && (
          <div className="completion-banner" role="status">
            <div className="completion-banner__title">¡Ejercicio completado!</div>
            <div className="completion-banner__meta">
              <span>
                <BoltIcon /> +{completion?.xpEarned ?? 0} XP
              </span>
              <span aria-label={`${completion?.stars ?? 0} estrellas`}>
                {[0, 1, 2].map((i) => (
                  <span
                    key={i}
                    className={i < (completion?.stars ?? 0) ? '' : 'completion-banner__star--empty'}
                  >
                    ★
                  </span>
                ))}
              </span>
            </div>
            <Link to={`/${categoryType}/${levelCode}`} className="btn btn--ghost">
              Volver a la lista
            </Link>
          </div>
        )}

        <div className="question-list">
          {data.questions.map((question, idx) => (
            <QuestionCard
              key={question.id}
              question={question}
              index={idx + 1}
              onAnswered={handleAnswered}
              disabled={completed}
            />
          ))}
        </div>
      </div>
    </section>
  );
}

export default ExercisePage;
