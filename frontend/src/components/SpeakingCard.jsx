import { useCallback, useEffect, useState } from 'react';
import { useSpeechRecognition } from '../hooks/useSpeechRecognition.js';
import { useTTS } from '../hooks/useTTS.js';

function MicIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden>
      <rect x="9" y="2" width="6" height="11" rx="3" />
      <path d="M5 10a7 7 0 0 0 14 0" />
      <path d="M12 19v3" />
    </svg>
  );
}

function SpeakerIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden>
      <path d="M11 5 6 9H2v6h4l5 4V5z" />
      <path d="M15.5 8.5a5 5 0 0 1 0 7" />
    </svg>
  );
}

function StopIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <rect x="6" y="6" width="12" height="12" rx="2" />
    </svg>
  );
}

export default function SpeakingCard({ question, onAnswer, disabled }) {
  const { isSupported, isListening, transcript, interim, error: recError, start, stop } = useSpeechRecognition();
  const { speak } = useTTS();
  const [confirmedTranscript, setConfirmedTranscript] = useState('');
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    setConfirmedTranscript('');
    setSubmitted(false);
  }, [question.id]);

  const handleMicClick = useCallback(() => {
    if (isListening) {
      stop();
      if (transcript) {
        setConfirmedTranscript(transcript);
      }
    } else {
      start();
    }
  }, [isListening, stop, start, transcript]);

  const handleSubmitTranscript = useCallback(() => {
    const text = confirmedTranscript || transcript;
    if (!text.trim()) return;
    setSubmitted(true);
    onAnswer(text.trim());
  }, [confirmedTranscript, transcript, onAnswer]);

  const handlePlay = useCallback(() => {
    if (question.audioText) {
      speak(question.audioText, 'en-US');
    }
  }, [question.audioText, speak]);

  const displayText = confirmedTranscript || transcript;
  const hasTranscript = displayText.trim().length > 0;

  if (!isSupported) {
    return (
      <div className="speaking-card">
        <p className="speaking-card__unsupported">
          El reconocimiento de voz no está disponible en este navegador.
          Usa Chrome o Edge para practicar speaking.
        </p>
      </div>
    );
  }

  return (
    <div className="speaking-card">
      <div className="speaking-card__header">
        <p className="speaking-card__instruction">
          Di la traducción en inglés en voz alta
        </p>
        {question.audioText && (
          <button
            type="button"
            className="btn btn--ghost btn--tiny"
            onClick={handlePlay}
            title="Escuchar pronunciación"
            disabled={disabled}
          >
            <SpeakerIcon /> Escuchar
          </button>
        )}
      </div>

      <div className="speaking-card__mic-area">
        <button
          type="button"
          className={`speaking-card__mic ${isListening ? 'speaking-card__mic--listening' : ''}`}
          onClick={handleMicClick}
          disabled={disabled || submitted}
          aria-label={isListening ? 'Detener grabación' : 'Empezar a hablar'}
        >
          {isListening ? <StopIcon /> : <MicIcon />}
        </button>
        {isListening && <span className="speaking-card__pulse" />}
        <span className="speaking-card__mic-label">
          {isListening ? 'Escuchando...' : 'Toca para hablar'}
        </span>
      </div>

      {recError && <p className="speaking-card__error">{recError}</p>}

      {isListening && interim && (
        <div className="speaking-card__transcript speaking-card__transcript--interim">
          {interim}
        </div>
      )}

      {hasTranscript && (
        <div className="speaking-card__transcript">
          <p className="speaking-card__transcript-label">Lo que dijiste:</p>
          <p className="speaking-card__transcript-text">{displayText}</p>
          {!submitted && (
            <button
              type="button"
              className="btn btn--primary"
              onClick={handleSubmitTranscript}
              disabled={disabled}
            >
              Confirmar respuesta
            </button>
          )}
        </div>
      )}
    </div>
  );
}
