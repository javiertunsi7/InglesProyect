import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTTS } from '../hooks/useTTS.js';
import { dictionaryService } from '../services/dictionaryService.js';

const CATEGORIES = [
  { value: '', label: 'Todas' },
  { value: 'GENERAL', label: 'General' },
  { value: 'TECH', label: 'Técnico' },
];

const LEVELS = ['', 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'];

function SpeakerIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M11 5 6 9H2v6h4l5 4V5zM15.5 8.5a5 5 0 0 1 0 7M18 6a8 8 0 0 1 0 12" />
    </svg>
  );
}

function DictionaryPage() {
  const { speak } = useTTS();
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('');
  const [level, setLevel] = useState('');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    const handle = setTimeout(() => {
      setLoading(true);
      setError(null);
      dictionaryService
        .search({ q: query, category: category || undefined, level: level || undefined, size: 30 })
        .then((value) => !cancelled && setData(value))
        .catch((err) => !cancelled && setError(err.message))
        .finally(() => !cancelled && setLoading(false));
    }, 250); // debounce simple sobre la búsqueda

    return () => {
      cancelled = true;
      clearTimeout(handle);
    };
  }, [query, category, level]);

  return (
    <section className="page">
      <div>
        <Link to="/" className="back-link">
          ← Volver al inicio
        </Link>
        <p className="eyebrow">Diccionario · Inglés general y técnico</p>
        <h1 className="display">Diccionario.</h1>
        <p className="lead" style={{ marginTop: '1rem' }}>
          Vocabulario clasificado por pista y nivel CEFR. Búsca palabra o significado.
        </p>

        <div className="dict-filters">
          <input
            type="search"
            className="dict-filters__search"
            placeholder="Buscar palabra o definición..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            aria-label="Buscar en el diccionario"
          />
          <div className="dict-filters__chips" role="radiogroup" aria-label="Pista">
            {CATEGORIES.map((c) => (
              <button
                key={c.value || 'all-cat'}
                type="button"
                role="radio"
                aria-checked={category === c.value}
                className={`chip${category === c.value ? ' chip--active' : ''}`}
                onClick={() => setCategory(c.value)}
              >
                {c.label}
              </button>
            ))}
          </div>
          <div className="dict-filters__chips" role="radiogroup" aria-label="Nivel">
            {LEVELS.map((lv) => (
              <button
                key={lv || 'all-lv'}
                type="button"
                role="radio"
                aria-checked={level === lv}
                className={`chip${level === lv ? ' chip--active' : ''}`}
                onClick={() => setLevel(lv)}
              >
                {lv || 'Todos'}
              </button>
            ))}
          </div>
        </div>

        {error && <p className="state state--error">{error}</p>}
        {loading && <p className="state">Cargando diccionario...</p>}

        {!loading && data && (
          <>
            <p className="muted" style={{ marginTop: '0.5rem', fontSize: '0.9rem' }}>
              {data.total} {data.total === 1 ? 'entrada' : 'entradas'}
            </p>
            <div className="dict-grid">
              {data.items.map((entry) => (
                <article key={entry.id} className="dict-card">
                  <header className="dict-card__head">
                    <h3 className="dict-card__word">{entry.word}</h3>
                    <span className="dict-card__tag">
                      {entry.categoryType === 'TECH' ? 'tech' : 'general'} · {entry.levelCode}
                    </span>
                  </header>
                  {(entry.phonetic || entry.partOfSpeech) && (
                    <p className="dict-card__meta">
                      {entry.phonetic && <span>{entry.phonetic}</span>}
                      {entry.partOfSpeech && (
                        <span className="dict-card__pos"> · {entry.partOfSpeech}</span>
                      )}
                    </p>
                  )}
                  <p className="dict-card__definition">{entry.definitionEs}</p>
                  {entry.exampleEn && (
                    <p className="dict-card__example">
                      <em>{entry.exampleEn}</em>
                      {entry.exampleEs && <br />}
                      {entry.exampleEs && <span className="muted">{entry.exampleEs}</span>}
                    </p>
                  )}
                  <button type="button" className="btn btn--ghost btn--tiny" onClick={() => speak(entry.word)}>
                    <SpeakerIcon /> Escuchar
                  </button>
                </article>
              ))}
            </div>
            {data.items.length === 0 && (
              <p className="state" style={{ marginTop: '2rem' }}>
                No hay resultados. Prueba con otra palabra o quita filtros.
              </p>
            )}
          </>
        )}
      </div>
    </section>
  );
}

export default DictionaryPage;
