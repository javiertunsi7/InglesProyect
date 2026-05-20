import { useEffect, useState } from 'react';
import { questService } from '../services/questService.js';

const QUEST_ICONS = {
  COMPLETE_EXERCISES: '📝',
  EARN_XP: '⚡',
  COMPLETE_REVIEWS: '📚',
  STREAK_MAINTENANCE: '🔥',
  PERFECT_SCORE: '⭐',
};

function DailyQuests() {
  const [quests, setQuests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    questService
      .getToday()
      .then((data) => {
        if (!cancelled) setQuests(data);
      })
      .catch(() => {})
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return (
      <div className="quests">
        <h3 className="quests__title">Misiones del día</h3>
        <p className="quests__loading">Cargando misiones...</p>
      </div>
    );
  }

  if (quests.length === 0) return null;

  const allDone = quests.every((q) => q.completed);

  return (
    <div className="quests">
      <div className="quests__header">
        <h3 className="quests__title">Misiones del día</h3>
        {allDone && <span className="quests__done-badge">✓ Completadas</span>}
      </div>
      <div className="quests__list">
        {quests.map((q) => (
          <div
            key={q.type}
            className={`quest${q.completed ? ' quest--done' : ''}`}
          >
            <span className="quest__icon" aria-hidden>
              {QUEST_ICONS[q.type] || '🎯'}
            </span>
            <div className="quest__body">
              <div className="quest__info">
                <span className="quest__desc">{q.description}</span>
                <span className="quest__reward">+{q.rewardXp} XP</span>
              </div>
              <div className="quest__bar">
                <div
                  className="quest__bar-fill"
                  style={{ width: `${Math.min(100, (q.progress / q.target) * 100)}%` }}
                />
              </div>
              <span className="quest__count">
                {q.progress}/{q.target}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default DailyQuests;
