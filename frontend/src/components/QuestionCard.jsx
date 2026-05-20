import { useState, useCallback } from 'react';
import { exerciseService } from '../services/exerciseService.js';
import { useTTS } from '../hooks/useTTS.js';
import SpeakingCard from './SpeakingCard.jsx';

const TYPE_LABELS = {
  TRANSLATION: 'Pregunta · Traducción',
  REVERSE_TRANSLATION: 'Pregunta · Traducción inversa',
  MULTIPLE_CHOICE: 'Pregunta · Opción múltiple',
  FILL_BLANK: 'Pregunta · Completa',
  LISTENING: 'Pregunta · Comprensión auditiva',
  WORD_ORDER: 'Pregunta · Ordena las palabras',
  MATCHING: 'Pregunta · Empareja',
  DICTATION: 'Pregunta · Dictado',
  SPEAKING: 'Pregunta · Speaking',
};

function HintIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M9 21h6M10 17h4M12 3a6 6 0 0 0-4 10.5c.5.5 1 1 1 1.5v1h6v-1c0-.5.5-1 1-1.5A6 6 0 0 0 12 3z" />
    </svg>
  );
}

function SpeakerIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M11 5 6 9H2v6h4l5 4V5zM15.5 8.5a5 5 0 0 1 0 7M18 6a8 8 0 0 1 0 12" />
    </svg>
  );
}

function MultipleChoice({ question, onSubmit, submitting, disabled }) {
  const [answer, setAnswer] = useState('');

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit(answer); }}>
      <div className="question-card__options">
        {question.options.map((option) => (
          <label
            key={option.id}
            className={`option${answer === option.value ? ' option--selected' : ''}`}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <span className="option__radio" aria-hidden>
                {answer === option.value ? '●' : ''}
              </span>
              {option.value}
            </span>
            <span className="option__letter">{option.label}</span>
            <input
              type="radio"
              name={`q-${question.id}`}
              value={option.value}
              checked={answer === option.value}
              onChange={(e) => setAnswer(e.target.value)}
              disabled={disabled}
              style={{ display: 'none' }}
            />
          </label>
        ))}
      </div>
      <div className="question-card__actions">
        <button type="submit" className="btn btn--primary" disabled={submitting || disabled || !answer}>
          {submitting ? 'Comprobando...' : 'Comprobar respuesta'}
        </button>
      </div>
    </form>
  );
}

function ListeningDictation({ question, onAnswer, onPlay, isDictation }) {
  const [answer, setAnswer] = useState('');

  return (
    <form onSubmit={(e) => { e.preventDefault(); onAnswer(answer); }}>
      <button type="button" className="btn btn--ghost audio-btn" onClick={onPlay}>
        <SpeakerIcon /> {isDictation ? 'Escuchar y escribir' : 'Escuchar'}
      </button>
      <input
        className="question-card__input"
        type="text"
        placeholder="Escribe tu respuesta..."
        value={answer}
        onChange={(e) => setAnswer(e.target.value)}
        autoComplete="off"
      />
      <div className="question-card__actions">
        <button type="submit" className="btn btn--primary">
          Comprobar respuesta
        </button>
      </div>
    </form>
  );
}

function WordOrder({ question, onAnswer }) {
  const [selected, setSelected] = useState([]);
  const words = question.availableWords || [];

  const toggleWord = (word) => {
    if (selected.includes(word)) {
      setSelected(selected.filter((w) => w !== word));
    } else {
      setSelected([...selected, word]);
    }
  };

  const resetOrder = () => setSelected([]);
  const answerStr = selected.join(' ');

  return (
    <form onSubmit={(e) => { e.preventDefault(); onAnswer(answerStr); }}>
      <div className="word-order__answer">
        {selected.length === 0 ? (
          <span className="word-order__placeholder">Toca las palabras en el orden correcto</span>
        ) : (
          selected.map((word, i) => (
            <span key={`${word}-${i}`} className="word-order__chip word-order__chip--selected" onClick={() => toggleWord(word)}>
              {word}
            </span>
          ))
        )}
      </div>
      <div className="word-order__pool">
        {words.filter((w) => !selected.includes(w)).map((word, i) => (
          <span key={`${word}-${i}`} className="word-order__chip" onClick={() => toggleWord(word)}>
            {word}
          </span>
        ))}
      </div>
      <div className="question-card__actions">
        <button type="submit" className="btn btn--primary" disabled={selected.length === 0}>
          Comprobar respuesta
        </button>
        {selected.length > 0 && (
          <button type="button" className="btn btn--ghost" onClick={resetOrder}>
            Reordenar
          </button>
        )}
      </div>
    </form>
  );
}

function Matching({ question, onAnswer }) {
  const [leftSelected, setLeftSelected] = useState(null);
  const [pairs, setPairs] = useState([]);
  const matchPairs = question.matchPairs || [];

  const leftItems = matchPairs.map((p) => ({ text: p.leftText, pairId: p.pairId }));
  const rightItems = matchPairs.map((p) => ({ text: p.rightText, pairId: p.pairId }));

  const selectLeft = (item) => {
    if (pairs.find((p) => p.pairId === item.pairId)) return;
    setLeftSelected(item);
  };

  const selectRight = (item) => {
    if (!leftSelected) return;
    if (pairs.find((p) => p.pairId === item.pairId)) return;
    const newPairs = [...pairs, { pairId: leftSelected.pairId, rightText: item.text }];
    setPairs(newPairs);
    setLeftSelected(null);
  };

  const removePair = (pairId) => {
    setPairs(pairs.filter((p) => p.pairId !== pairId));
  };

  const allPaired = pairs.length === matchPairs.length;
  const answerStr = pairs.map((p) => p.pairId).join(',');

  return (
    <form onSubmit={(e) => { e.preventDefault(); onAnswer(answerStr); }}>
      <div className="matching">
        <div className="matching__column">
          {leftItems.map((item) => {
            const paired = pairs.find((p) => p.pairId === item.pairId);
            return (
              <div
                key={item.pairId}
                className={`matching__item${leftSelected?.pairId === item.pairId ? ' matching__item--active' : ''}${paired ? ' matching__item--paired' : ''}`}
                onClick={() => selectLeft(item)}
              >
                {item.text}
              </div>
            );
          })}
        </div>
        <div className="matching__column">
          {rightItems.map((item) => {
            const paired = pairs.find((p) => p.pairId === item.pairId || p.rightText === item.text);
            return (
              <div
                key={item.pairId}
                className={`matching__item${paired ? ' matching__item--paired' : ''}`}
                onClick={() => selectRight(item)}
              >
                {paired ? '' : item.text}
              </div>
            );
          })}
        </div>
      </div>
      {pairs.length > 0 && (
        <div className="matching__pairs">
          {pairs.map((p) => {
            const left = leftItems.find((l) => l.pairId === p.pairId);
            return (
              <span key={p.pairId} className="matching__pair-chip" onClick={() => removePair(p.pairId)}>
                {left?.text} ↔ {p.rightText} ✕
              </span>
            );
          })}
        </div>
      )}
      <div className="question-card__actions">
        <button type="submit" className="btn btn--primary" disabled={!allPaired}>
          Comprobar respuesta
        </button>
      </div>
    </form>
  );
}

function TextAnswer({ question, onSubmit, submitting, disabled }) {
  const [answer, setAnswer] = useState('');

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit(answer); }}>
      <input
        className="question-card__input"
        type="text"
        placeholder="Escribe tu respuesta..."
        value={answer}
        onChange={(e) => setAnswer(e.target.value)}
        disabled={disabled}
        autoComplete="off"
      />
      <div className="question-card__actions">
        <button type="submit" className="btn btn--primary" disabled={submitting || disabled}>
          {submitting ? 'Comprobando...' : 'Comprobar respuesta'}
        </button>
      </div>
    </form>
  );
}

function renderPrompt(prompt, highlight) {
  if (!highlight) return prompt;
  const safe = highlight.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const regex = new RegExp(`('?)(${safe})('?)`);
  const match = prompt.match(regex);
  if (!match) return prompt;
  const [full] = match;
  const start = match.index;
  return (
    <>
      {prompt.slice(0, start)}
      <em>{full.includes("'") ? `'${highlight}'` : highlight}</em>
      {prompt.slice(start + full.length)}
    </>
  );
}

function QuestionCard({ question, index, onAnswered, disabled }) {
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState(null);
  const [showHint, setShowHint] = useState(false);
  const [error, setError] = useState(null);
  const { speak } = useTTS();

  const isAnswered = feedback?.correct === true || (question.answered && question.lastAnswerCorrect);
  const chipLabel = TYPE_LABELS[question.type] ?? 'Pregunta';

  const handlePlay = useCallback(() => {
    speak(question.audioText || question.prompt, 'en-US');
  }, [question.audioText, question.prompt, speak]);

  const handleAnswer = async (answer) => {
    if (!answer || !answer.trim()) {
      setError('Escribe una respuesta antes de enviar.');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const result = await exerciseService.submitAnswer(question.id, answer.trim());
      setFeedback(result);
      onAnswered?.(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (feedback || (question.answered && question.lastAnswerCorrect)) {
    const fb = feedback || { correct: true, message: 'Resuelto', correctAnswer: question.correctAnswer, explanation: question.explanation };
    return (
      <article className={`question-card${fb.correct ? ' question-card--done' : ''}`}>
        <span className="question-card__chip">
          {chipLabel.replace('Pregunta · ', `Pregunta ${index} · `)}
        </span>
        <div className={`feedback feedback--${fb.correct ? 'ok' : 'ko'}`}>
          <strong>{fb.message}</strong>
          {fb.correct && fb.correctAnswer && (
            <p style={{ marginTop: '0.35rem' }}>
              Respuesta: <em>{fb.correctAnswer}</em>
            </p>
          )}
          {fb.explanation && (
            <p className="feedback__explanation">{fb.explanation}</p>
          )}
        </div>
      </article>
    );
  }

  return (
    <article className="question-card">
      <span className="question-card__chip">
        {chipLabel.replace('Pregunta · ', `Pregunta ${index} · `)}
      </span>

      {question.type !== 'DICTATION' && (
        <h3 className="question-card__prompt">
          {renderPrompt(question.prompt, question.promptHighlight)}
        </h3>
      )}

      {question.type === 'DICTATION' && !feedback && (
        <p className="question-card__context muted">Escucha el audio y escribe lo que oyes.</p>
      )}

      {question.context && <p className="question-card__context">{question.context}</p>}

      {question.type === 'MULTIPLE_CHOICE' && (
        <MultipleChoice question={question} onSubmit={handleAnswer} submitting={submitting} disabled={disabled} />
      )}

      {(question.type === 'LISTENING' || question.type === 'DICTATION') && (
        <ListeningDictation question={question} onAnswer={handleAnswer} onPlay={handlePlay} isDictation={question.type === 'DICTATION'} />
      )}

      {question.type === 'WORD_ORDER' && (
        <WordOrder question={question} onAnswer={handleAnswer} />
      )}

      {question.type === 'MATCHING' && (
        <Matching question={question} onAnswer={handleAnswer} />
      )}

      {question.type === 'SPEAKING' && (
        <SpeakingCard question={question} onAnswer={handleAnswer} disabled={disabled} />
      )}

      {!['MULTIPLE_CHOICE', 'LISTENING', 'DICTATION', 'WORD_ORDER', 'MATCHING', 'SPEAKING'].includes(question.type) && (
        <TextAnswer question={question} onSubmit={handleAnswer} submitting={submitting} disabled={disabled} />
      )}

      {question.hint && !['LISTENING', 'DICTATION', 'WORD_ORDER', 'MATCHING', 'SPEAKING'].includes(question.type) && !showHint && (
        <div className="question-card__actions">
          <button type="button" className="btn btn--ghost" onClick={() => setShowHint(true)} disabled={isAnswered}>
            <HintIcon /> Ver pista
          </button>
        </div>
      )}

      {showHint && question.hint && (
        <p className="feedback" style={{ background: 'rgba(232, 200, 120, 0.1)', color: 'var(--warning)', border: '1px solid rgba(232, 200, 120, 0.25)' }}>
          <strong>Pista: </strong>{question.hint}
        </p>
      )}
      {error && <p className="alert alert--error">{error}</p>}
    </article>
  );
}

export default QuestionCard;