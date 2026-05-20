import { useEffect, useState } from 'react';
import { statsService } from '../services/statsService.js';

const TYPE_LABELS = {
  TRANSLATION: 'Traducción',
  REVERSE_TRANSLATION: 'Inversa',
  MULTIPLE_CHOICE: 'Opción múltiple',
  FILL_BLANK: 'Rellenar',
  LISTENING: 'Listening',
  WORD_ORDER: 'Ordenar',
  MATCHING: 'Emparejar',
  DICTATION: 'Dictado',
};

const TYPE_COLORS = {
  TRANSLATION: '#6c43d9',
  REVERSE_TRANSLATION: '#e87b6d',
  MULTIPLE_CHOICE: '#48bb78',
  FILL_BLANK: '#f6ad55',
  LISTENING: '#4299e1',
  WORD_ORDER: '#ed8936',
  MATCHING: '#9f7aea',
  DICTATION: '#fc8181',
};

function StatBar({ label, value, max, color }) {
  const pct = max > 0 ? (value / max) * 100 : 0;
  return (
    <div className="stat-bar">
      <div className="stat-bar__head">
        <span className="stat-bar__label">{label}</span>
        <span className="stat-bar__value">{value} XP</span>
      </div>
      <div className="stat-bar__track">
        <div className="stat-bar__fill" style={{ width: `${pct}%`, background: color }} />
      </div>
    </div>
  );
}

function StatsSection() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    statsService
      .getStats()
      .then((data) => {
        if (!cancelled) setStats(data);
      })
      .catch(() => {})
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return (
      <div className="stats-section">
        <h2 className="section-title">Estadísticas</h2>
        <p className="muted">Cargando estadísticas...</p>
      </div>
    );
  }

  if (!stats) return null;

  const maxXpByType = Math.max(1, ...stats.xpByType.map((t) => t.xp));
  const maxWeekly = Math.max(1, ...stats.weeklyXp.map((d) => d.xp));
  const maxMonthly = Math.max(1, ...stats.monthlyXp.map((m) => m.xp));

  return (
    <div className="stats-section">
      <h2 className="section-title" style={{ marginTop: '3rem' }}>Estadísticas</h2>

      {/* Precisión de hoy */}
      <div className="stats-grid">
        <div className="stat-card">
          <span className="stat-card__label">Precisión hoy</span>
          <span className="stat-card__value">
            {stats.todayAccuracy.total > 0
              ? `${stats.todayAccuracy.percent}%`
              : '—'}
          </span>
          <span className="stat-card__sub">
            {stats.todayAccuracy.correct}/{stats.todayAccuracy.total} aciertos
          </span>
        </div>
      </div>

      {/* XP por tipo de pregunta */}
      <h3 className="stats-subtitle">XP por tipo de pregunta</h3>
      <div className="stat-bars">
        {stats.xpByType.map((t) => (
          <StatBar
            key={t.type}
            label={TYPE_LABELS[t.type] || t.type}
            value={t.xp}
            max={maxXpByType}
            color={TYPE_COLORS[t.type] || '#6c43d9'}
          />
        ))}
        {stats.xpByType.length === 0 && (
          <p className="muted" style={{ fontSize: '0.85rem' }}>Completa ejercicios para ver estadísticas por tipo.</p>
        )}
      </div>

      {/* XP semanal */}
      <h3 className="stats-subtitle">XP esta semana</h3>
      <div className="stat-bars">
        {stats.weeklyXp.map((d) => (
          <StatBar
            key={d.label}
            label={d.label}
            value={d.xp}
            max={maxWeekly}
            color="#e87b6d"
          />
        ))}
      </div>

      {/* XP mensual */}
      <h3 className="stats-subtitle">XP por mes</h3>
      <div className="stat-bars">
        {stats.monthlyXp.map((m) => (
          <StatBar
            key={m.month}
            label={m.month}
            value={m.xp}
            max={maxMonthly}
            color="#6c43d9"
          />
        ))}
      </div>
    </div>
  );
}

export default StatsSection;
