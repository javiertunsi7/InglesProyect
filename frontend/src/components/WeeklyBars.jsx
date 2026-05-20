/**
 * Mini gráfico de barras vertical para la actividad diaria.
 * Cada barra recibe {dayLabel, xpEarned, minutesPracticed?, active}.
 *
 * @param {{ days: Array }} props
 */
function WeeklyBars({ days, ariaLabel = 'Actividad' }) {
  if (!days || days.length === 0) {
    return null;
  }
  const max = Math.max(1, ...days.map((d) => d.xpEarned ?? 0));
  return (
    <div className="weekbar" aria-label={ariaLabel}>
      {days.map((day, idx) => {
        const heightPct = ((day.xpEarned ?? 0) / max) * 100;
        return (
          <span
            key={idx}
            className={`weekbar__bar${day.active ? ' weekbar__bar--active' : ''}`}
            style={{ height: `${Math.max(heightPct, 8)}%` }}
            title={`${day.dayLabel} · ${day.xpEarned ?? 0} XP`}
          />
        );
      })}
    </div>
  );
}

export default WeeklyBars;
