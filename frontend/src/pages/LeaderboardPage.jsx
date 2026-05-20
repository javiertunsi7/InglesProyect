import { useAuth } from '../context/AuthContext.jsx';
import { useFetch } from '../hooks/useFetch.js';
import { leaderboardService } from '../services/leaderboardService.js';

function TrophyIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M6 9H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h2M18 9h2a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2h-2M6 20h12M12 16v4M10 9l2 3 2-3M6 9a6 6 0 0 0 12 0" />
    </svg>
  );
}

function Medals() {
  return {
    1: <span className="leaderboard__medal leaderboard__medal--gold">🥇</span>,
    2: <span className="leaderboard__medal leaderboard__medal--silver">🥈</span>,
    3: <span className="leaderboard__medal leaderboard__medal--bronze">🥉</span>,
  };
}

function LeaderboardPage() {
  const { user } = useAuth();
  const { data, loading, error } = useFetch(leaderboardService.getLeaderboard);
  const medals = Medals();

  if (loading) return <section className="page"><p className="state">Cargando...</p></section>;
  if (error) return <section className="page"><p className="state state--error">{error}</p></section>;
  if (!data) return null;

  const { entries, currentUser } = data;

  return (
    <section className="page">
      <div className="leaderboard">
        <div className="leaderboard__hero">
          <TrophyIcon />
          <h1 className="display">Clasificación</h1>
          <p className="lead">Los mejores estudiantes según su XP acumulado.</p>
        </div>

        <div className="leaderboard__table-wrap">
          <table className="leaderboard__table">
            <thead>
              <tr>
                <th className="leaderboard__th--rank">#</th>
                <th>Estudiante</th>
                <th className="leaderboard__th--num">XP</th>
                <th className="leaderboard__th--num">Racha</th>
              </tr>
            </thead>
            <tbody>
              {entries.length === 0 && (
                <tr>
                  <td colSpan={4} className="muted" style={{ textAlign: 'center', padding: '2rem' }}>
                    Aún no hay datos de clasificación.
                  </td>
                </tr>
              )}
              {entries.map((entry) => {
                const isMe = user && entry.userId === user.id;
                return (
                  <tr
                    key={entry.userId}
                    className={`leaderboard__row${isMe ? ' leaderboard__row--me' : ''}`}
                  >
                    <td className="leaderboard__rank">
                      {entry.rank <= 3 ? (
                        medals[entry.rank]
                      ) : (
                        <span className="mono">#{entry.rank}</span>
                      )}
                    </td>
                    <td>
                      <span className="leaderboard__avatar">{entry.initials}</span>
                      <span className="leaderboard__name">{entry.displayName}</span>
                      {isMe && <span className="chip chip--user">tú</span>}
                    </td>
                    <td className="leaderboard__num mono">
                      {entry.totalXp.toLocaleString('es-ES')}
                    </td>
                    <td className="leaderboard__num mono">
                      {entry.currentStreak > 0 ? `${entry.currentStreak} días` : '—'}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {currentUser && currentUser.rank > entries.length && (
          <div className="leaderboard__current-user">
            <table className="leaderboard__table">
              <tbody>
                <tr className="leaderboard__row leaderboard__row--me">
                  <td className="leaderboard__rank">
                    <span className="mono">#{currentUser.rank}</span>
                  </td>
                  <td>
                    <span className="leaderboard__avatar">{currentUser.initials}</span>
                    <span className="leaderboard__name">{currentUser.displayName}</span>
                    <span className="chip chip--user">tú</span>
                  </td>
                  <td className="leaderboard__num mono">
                    {currentUser.totalXp.toLocaleString('es-ES')}
                  </td>
                  <td className="leaderboard__num mono">
                    {currentUser.currentStreak > 0 ? `${currentUser.currentStreak} días` : '—'}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}

export default LeaderboardPage;