import { Link, useNavigate, useParams } from 'react-router-dom';
import { useFetch } from '../hooks/useFetch.js';
import { levelService } from '../services/levelService.js';
import WeeklyBars from '../components/WeeklyBars.jsx';

function BackIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M19 12H5M11 6l-6 6 6 6" />
    </svg>
  );
}

function pad(n) {
  return n.toString().padStart(2, '0');
}

function StateLabel({ status }) {
  if (status === 'COMPLETED') return null;
  const label =
    status === 'IN_PROGRESS' ? 'En curso' : status === 'LOCKED' ? 'Bloqueado' : 'Disponible';
  return <span className="exercise-cell__state">{label}</span>;
}

function Stars({ count }) {
  return (
    <span className="exercise-cell__stars" aria-label={`${count} estrellas`}>
      {[0, 1, 2].map((i) => (
        <span key={i} className={i < count ? '' : 'exercise-cell__star--empty'}>
          ★
        </span>
      ))}
    </span>
  );
}

function ExerciseCell({ exercise, categoryType, levelCode }) {
  const baseClass = 'exercise-cell';
  const modifier =
    exercise.status === 'COMPLETED'
      ? ' exercise-cell--completed'
      : exercise.status === 'IN_PROGRESS'
        ? ' exercise-cell--current'
        : exercise.status === 'LOCKED'
          ? ' exercise-cell--locked'
          : '';
  const Container = exercise.status === 'LOCKED' ? 'div' : Link;
  const props =
    exercise.status === 'LOCKED'
      ? { className: `${baseClass}${modifier}` }
      : {
          className: `${baseClass}${modifier}`,
          to: `/${categoryType}/${levelCode}/${exercise.id}`,
        };
  return (
    <Container {...props}>
      <h4 className="exercise-cell__number">{pad(exercise.position)}</h4>
      <Stars count={exercise.stars ?? 0} />
      <StateLabel status={exercise.status} />
    </Container>
  );
}

function ProgressRing({ percent }) {
  const radius = 38;
  const circumference = 2 * Math.PI * radius;
  const dash = (percent / 100) * circumference;
  return (
    <svg width="96" height="96" viewBox="0 0 96 96" aria-hidden>
      <circle
        cx="48"
        cy="48"
        r={radius}
        stroke="rgba(255,255,255,0.06)"
        strokeWidth="6"
        fill="none"
      />
      <circle
        cx="48"
        cy="48"
        r={radius}
        stroke="url(#ring-grad)"
        strokeWidth="6"
        fill="none"
        strokeLinecap="round"
        strokeDasharray={`${dash} ${circumference - dash}`}
        transform="rotate(-90 48 48)"
      />
      <defs>
        <linearGradient id="ring-grad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#e87b6d" />
          <stop offset="100%" stopColor="#f5b07a" />
        </linearGradient>
      </defs>
    </svg>
  );
}

function SubLevelPage() {
  const { categoryType, levelCode } = useParams();
  const navigate = useNavigate();
  const { data, loading, error } = useFetch(
    () => levelService.getDetail(categoryType, levelCode),
    [categoryType, levelCode],
  );

  if (loading) return <p className="state">Cargando ejercicios...</p>;
  if (error) return <p className="state state--error">{error}</p>;
  if (!data) return null;

  const activeDays = data.weeklyActivity.filter((d) => d.active).length;

  return (
    <section className="page page--split">
      <div>
        <Link to={`/${categoryType}`} className="back-link">
          <BackIcon /> Volver a niveles
        </Link>
        <p className="eyebrow">
          {data.categoryName} · Nivel {data.code} · {data.displayName}
        </p>
        <h1 className="display">
          {data.displayName}, <em>paso a paso.</em>
        </h1>
        <p className="lead" style={{ marginTop: '1rem' }}>
          Has completado {data.completedExercises} de {data.totalExercises} ejercicios. Cada bloque
          introduce vocabulario y gramática nueva y construye sobre el anterior.
        </p>

        <div className="block-list" style={{ marginTop: '2.5rem' }}>
          {data.blocks.map((block) => (
            <section key={block.id} className="block">
              <div className="block__header">
                <span className="block__index">{pad(block.position)}</span>
                <div>
                  <h2 className="block__title">{block.title}</h2>
                  <span className="block__subtitle">{block.subtitle}</span>
                </div>
              </div>
              <div className="exercise-grid">
                {block.exercises.map((exercise) => (
                  <ExerciseCell
                    key={exercise.id}
                    exercise={exercise}
                    categoryType={categoryType}
                    levelCode={levelCode}
                  />
                ))}
              </div>
            </section>
          ))}
        </div>
      </div>

      <aside className="sidebar">
        <article className="card">
          <p className="eyebrow eyebrow--muted">Tu progreso</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div className="progress-ring">
              <ProgressRing percent={data.progressPercent} />
              <div className="progress-ring__number">
                {data.completedExercises}
                <span className="progress-ring__sub">/{data.totalExercises}</span>
              </div>
            </div>
            <div>
              <p style={{ margin: 0 }}>Ejercicios completados</p>
              <p className="muted" style={{ margin: '0.25rem 0 0', fontSize: '0.85rem' }}>
                {data.progressPercent}% del nivel
              </p>
            </div>
          </div>
        </article>

        {data.nextExercise && (
          <article className="card next-card">
            <p className="eyebrow eyebrow--muted">Siguiente</p>
            <h3 className="card__title">Ejercicio {data.nextExercise.position}</h3>
            <p className="card__sub">
              {data.nextExercise.topic ?? 'Vocabulario'} · {data.nextExercise.questionsCount} preguntas
              con feedback inmediato.
            </p>
            <button
              type="button"
              className="btn btn--primary btn--block"
              onClick={() =>
                navigate(`/${categoryType}/${levelCode}/${data.nextExercise.id}`)
              }
            >
              Empezar
            </button>
          </article>
        )}

        <article className="card">
          <p className="eyebrow eyebrow--muted">Esta semana</p>
          <p style={{ margin: 0 }}>
            <span style={{ fontFamily: 'var(--font-display)', fontSize: '1.8rem' }}>
              {activeDays}
            </span>
            <span className="muted">/7</span>
          </p>
          <p className="muted" style={{ marginTop: '0.25rem', fontSize: '0.85rem' }}>
            días esta semana
          </p>
          <div style={{ marginTop: '0.75rem' }}>
            <WeeklyBars days={data.weeklyActivity} ariaLabel="Actividad semanal" />
          </div>
        </article>
      </aside>
    </section>
  );
}

export default SubLevelPage;
