/**
 * Gráfico de barras para el forecast del SRS: cuántos repasos te tocarán
 * cada día durante los próximos N días.
 *
 * Recibe `forecast: [{date, count}]` (el shape que devuelve el backend en
 * {@code ProgressOverviewResponse.srsForecast}). La primera entrada es hoy.
 */
const DAY_INITIAL_ES = ['D', 'L', 'M', 'X', 'J', 'V', 'S'];

function parseLocalDate(value) {
  // value puede ser "2026-05-15" o ya un Date.
  if (value instanceof Date) return value;
  if (typeof value === 'string') {
    const [y, m, d] = value.split('-').map(Number);
    return new Date(y, (m ?? 1) - 1, d ?? 1);
  }
  return new Date(value);
}

function ForecastBars({ forecast, ariaLabel = 'Próximos repasos' }) {
  if (!forecast || forecast.length === 0) {
    return (
      <p className="muted" style={{ fontSize: '0.85rem' }}>
        No hay repasos programados todavía. Responde a algunas preguntas y vuelve.
      </p>
    );
  }
  const max = Math.max(1, ...forecast.map((d) => Number(d.count ?? 0)));
  const todayStr = new Date().toISOString().slice(0, 10);

  return (
    <div className="forecast" aria-label={ariaLabel}>
      {forecast.map((entry, idx) => {
        const date = parseLocalDate(entry.date);
        const count = Number(entry.count ?? 0);
        const heightPct = (count / max) * 100;
        const dayInitial = DAY_INITIAL_ES[date.getDay()];
        const dayNum = date.getDate();
        const isToday =
          typeof entry.date === 'string' ? entry.date.startsWith(todayStr) : false;
        return (
          <div key={idx} className="forecast__col" title={`${count} repasos · ${entry.date}`}>
            <div className="forecast__bar-wrap">
              <span
                className={`forecast__bar${count > 0 ? ' forecast__bar--active' : ''}`}
                style={{ height: `${Math.max(heightPct, count > 0 ? 12 : 4)}%` }}
              />
            </div>
            <span className={`forecast__label${isToday ? ' forecast__label--today' : ''}`}>
              {dayInitial}
              <small>{dayNum}</small>
            </span>
          </div>
        );
      })}
    </div>
  );
}

export default ForecastBars;
