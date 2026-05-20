import { useCallback, useRef } from 'react';

export function useTTS() {
  const currentRef = useRef(null);

  const speak = useCallback((text, lang = 'en-US') => {
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = lang;
    utterance.rate = 0.9;
    currentRef.current = utterance;
    window.speechSynthesis.speak(utterance);
  }, []);

  const stop = useCallback(() => {
    window.speechSynthesis.cancel();
    currentRef.current = null;
  }, []);

  return { speak, stop };
}
