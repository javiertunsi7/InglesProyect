import { Link, useParams } from 'react-router-dom';
import { useFetch } from '../hooks/useFetch.js';
import { categoryService } from '../services/categoryService.js';
import { levelService } from '../services/levelService.js';

const PREMIUM_LEVELS = ['C1', 'C2'];

function ClockIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg className="locked-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <rect x="5" y="11" width="14" height="10" rx="2" />
      <path d="M8 11V8a4 4 0 1 1 8 0v3" />
    </svg>
  );
}

function PremiumIcon() {
  return (
    <svg className="premium-icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
    </svg>
  );
}

function BackIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M19 12H5M11 6l-6 6 6 6" />
    </svg>
  );
}

function LevelSelectionPage() {
  const { categoryType } = useParams();
  const { data: category } = useFetch(() => categoryService.getByType(categoryType), [categoryType]);
  const { data: levels, loading, error } = useFetch(
    () => levelService.getByCategory(categoryType),
    [categoryType],
  );

  if (loading) return <p className="state">Cargando niveles...</p>;
  if (error) return <p className="state state--error">{error}</p>;
  if (!levels) return null;

  const total = levels.length;
  return (
    <section className="page">
      <div>
        <Link to="/" className="back-link">
          <BackIcon /> Volver a las pistas
        </Link>
        <p className="eyebrow">
          {category?.displayName ?? 'Pista'} · {total} niveles CEFR
        </p>
        <h1 className="display">Elige tu nivel.</h1>
        <p className="lead" style={{ marginTop: '1rem' }}>
          Selecciona el nivel CEFR que mejor se adapte a ti. Si dudas, empieza por A1 y avanza a tu
          ritmo — los siguientes se desbloquean al completar el anterior.
        </p>
      </div>

      <div className="level-grid">
        {levels.map((level) => (
          <LevelCard key={level.id} level={level} categoryType={categoryType} />
        ))}
      </div>
    </section>
  );
}

function LevelCard({ level, categoryType }) {
  const isPremiumLevel = PREMIUM_LEVELS.includes(level.code);
  const completed = level.locked
    ? 'BLOQUEADO'
    : `${level.completedExercises}/${level.totalExercises} ejercicios`;
  const percent = level.locked ? 0 : level.progressPercent;

  if (level.locked && isPremiumLevel) {
    return (
      <Link to="/precios" className="level-card level-card--premium">
        <PremiumIcon />
        <div>
          <div className="level-card__head">
            <span className="level-card__badge">{level.code}</span>
            <span className="level-card__hours">
              <ClockIcon /> {level.estimatedHours} h
            </span>
          </div>
          <h3 className="level-card__title">{level.displayName}</h3>
          <p className="level-card__desc">{level.description}</p>
        </div>
        <div className="premium-lock">
          <span className="premium-lock__badge">Premium</span>
          <span className="premium-lock__cta">Desbloquear</span>
        </div>
      </Link>
    );
  }

  const Container = level.locked ? 'div' : Link;
  const props = level.locked
    ? { className: 'level-card level-card--locked' }
    : { className: 'level-card', to: `/${categoryType}/${level.code}` };

  return (
    <Container {...props}>
      {level.locked && <LockIcon />}
      <div>
        <div className="level-card__head">
          <span className="level-card__badge">{level.code}</span>
          <span className="level-card__hours">
            <ClockIcon /> {level.estimatedHours} h
          </span>
        </div>
        <h3 className="level-card__title">{level.displayName}</h3>
        <p className="level-card__desc">{level.description}</p>
      </div>
      <div>
        <div className="progress-row">
          <span>{completed}</span>
          <span>{percent}%</span>
        </div>
        <div className="progress-bar">
          <div className="progress-bar__fill" style={{ width: `${percent}%` }} />
        </div>
      </div>
    </Container>
  );
}

export default LevelSelectionPage;
