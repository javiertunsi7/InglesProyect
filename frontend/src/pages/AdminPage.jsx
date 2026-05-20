import { useState, useEffect, useCallback } from 'react';
import { adminService } from '../services/adminService.js';

function UsersIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM20 8v6M17 11h6" />
    </svg>
  );
}

function BookIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H19M4 19.5A2.5 2.5 0 0 0 6.5 22H19V2H6.5A2.5 2.5 0 0 0 4 4.5v15z" />
    </svg>
  );
}

function DictionaryIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M12 6h6M12 10h6M12 14h6M4 19.5A2.5 2.5 0 0 1 6.5 17H19V2H6.5A2.5 2.5 0 0 0 4 4.5v15z" />
    </svg>
  );
}

function StarIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" strokeWidth="1" aria-hidden>
      <path d="M12 2l3.1 6.3 6.9 1-5 4.9 1.2 6.9L12 17.8 5.8 21l1.2-6.9-5-4.9 6.9-1L12 2z" />
    </svg>
  );
}

const TABS = [
  { id: 'panel', label: 'Panel' },
  { id: 'users', label: 'Usuarios' },
  { id: 'exercises', label: 'Ejercicios' },
  { id: 'dictionary', label: 'Diccionario' },
  { id: 'words', label: 'WOTD' },
];

function StatCard({ title, value, icon }) {
  return (
    <div className="admin-stat-card">
      <span className="admin-stat-card__icon">{icon}</span>
      <div className="admin-stat-card__body">
        <span className="admin-stat-card__value">{value}</span>
        <span className="admin-stat-card__label">{title}</span>
      </div>
    </div>
  );
}

function AdminPage() {
  const [activeTab, setActiveTab] = useState('panel');
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [exercises, setExercises] = useState([]);
  const [dictionary, setDictionary] = useState([]);
  const [words, setWords] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    try {
      const [s, u, e, d, w] = await Promise.all([
        adminService.getStats(),
        adminService.getUsers(),
        adminService.getExercises(),
        adminService.getDictionary(),
        adminService.getWordsOfDay(),
      ]);
      setStats(s);
      setUsers(u);
      setExercises(e);
      setDictionary(d);
      setWords(w);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  if (loading) return <section className="page"><p className="state">Cargando...</p></section>;

  return (
    <section className="page">
      <div className="admin">
        <h1 className="display">Admin</h1>

        <div className="admin-tabs">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              type="button"
              className={`admin-tabs__btn${activeTab === tab.id ? ' admin-tabs__btn--active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {activeTab === 'panel' && (
          <div className="admin-stats">
            <StatCard title="Usuarios" value={stats.totalUsers} icon={<UsersIcon />} />
            <StatCard title="Admins" value={stats.totalAdminUsers} icon={<StarIcon />} />
            <StatCard title="Ejercicios" value={stats.totalExercises} icon={<BookIcon />} />
            <StatCard title="Diccionario" value={stats.totalDictionaryEntries} icon={<DictionaryIcon />} />
          </div>
        )}

        {activeTab === 'users' && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Email</th>
                  <th>Nombre</th>
                  <th>Rol</th>
                  <th>XP</th>
                  <th>Racha</th>
                  <th>Registro</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td className="mono">{u.id}</td>
                    <td>{u.email}</td>
                    <td>{u.displayName}</td>
                    <td><span className={`chip chip--${u.role === 'ADMIN' ? 'admin' : 'user'}`}>{u.role}</span></td>
                    <td className="mono">{u.totalXp?.toLocaleString('es-ES')}</td>
                    <td className="mono">{u.currentStreak} días</td>
                    <td className="muted">{new Date(u.createdAt).toLocaleDateString('es-ES')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'exercises' && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Título</th>
                  <th>Level ID</th>
                  <th>Block ID</th>
                  <th>Pos.</th>
                  <th>Preguntas</th>
                  <th>Min.</th>
                  <th>XP</th>
                  <th>Bloqueado</th>
                </tr>
              </thead>
              <tbody>
                {exercises.map((ex) => (
                  <tr key={ex.id}>
                    <td className="mono">{ex.id}</td>
                    <td>{ex.title}</td>
                    <td className="mono">{ex.levelId}</td>
                    <td className="mono">{ex.blockId}</td>
                    <td className="mono">{ex.position}</td>
                    <td className="mono">{ex.questionsCount}</td>
                    <td className="mono">{ex.estimatedMinutes}</td>
                    <td className="mono">{ex.xpReward}</td>
                    <td>{ex.locked ? '🔒' : '✓'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'dictionary' && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Palabra</th>
                  <th>Fonética</th>
                  <th>Definición (ES)</th>
                  <th>Categoría</th>
                  <th>Nivel</th>
                </tr>
              </thead>
              <tbody>
                {dictionary.map((e) => (
                  <tr key={e.id}>
                    <td className="mono">{e.id}</td>
                    <td><strong>{e.word}</strong></td>
                    <td className="muted">{e.phonetic}</td>
                    <td className="muted">{e.definitionEs}</td>
                    <td>{e.categoryType}</td>
                    <td>{e.levelCode}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'words' && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Fecha</th>
                  <th>Palabra</th>
                  <th>Definición (ES)</th>
                  <th>Ejemplo (EN)</th>
                </tr>
              </thead>
              <tbody>
                {words.map((w) => (
                  <tr key={w.id}>
                    <td className="mono">{w.id}</td>
                    <td>{new Date(w.onDate).toLocaleDateString('es-ES')}</td>
                    <td><strong>{w.word}</strong></td>
                    <td className="muted">{w.definitionEs}</td>
                    <td className="muted italic">{w.exampleEn}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}

export default AdminPage;