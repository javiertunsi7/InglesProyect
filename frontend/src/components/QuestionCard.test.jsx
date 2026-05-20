import { render, screen, act } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import QuestionCard from './QuestionCard.jsx';

vi.mock('../services/exerciseService.js', () => ({
  exerciseService: {
    submitAnswer: vi.fn().mockResolvedValue({
      correct: true,
      message: '¡Correcto!',
      correctAnswer: 'hello',
      explanation: 'Saludo común.',
      exerciseStatus: 'IN_PROGRESS',
      exerciseCompleted: false,
      xpEarned: 10,
    }),
  },
}));

const translationQuestion = {
  id: 1,
  position: 1,
  type: 'TRANSLATION',
  prompt: 'Translate "hello"',
  promptHighlight: 'hello',
  hint: 'It is a greeting',
  answered: false,
  lastAnswerCorrect: false,
};

const mcQuestion = {
  id: 2,
  position: 2,
  type: 'MULTIPLE_CHOICE',
  prompt: 'What does "hello" mean?',
  options: [
    { id: 1, label: 'A', value: 'Adiós' },
    { id: 2, label: 'B', value: 'Hola' },
    { id: 3, label: 'C', value: 'Gracias' },
  ],
  answered: false,
  lastAnswerCorrect: false,
};

const listeningQuestion = {
  id: 3,
  position: 3,
  type: 'LISTENING',
  prompt: 'Listen and type what you hear',
  audioText: 'Good morning',
  answered: false,
  lastAnswerCorrect: false,
};

const wordOrderQuestion = {
  id: 4,
  position: 4,
  type: 'WORD_ORDER',
  prompt: 'Order the words',
  correctAnswer: 'I am a student',
  availableWords: ['student', 'a', 'I', 'am'],
  answered: false,
  lastAnswerCorrect: false,
};

const matchingQuestion = {
  id: 5,
  position: 5,
  type: 'MATCHING',
  prompt: 'Match the pairs',
  matchPairs: [
    { pairId: '1', leftText: 'hello', rightText: 'hola' },
    { pairId: '2', leftText: 'goodbye', rightText: 'adiós' },
  ],
  answered: false,
  lastAnswerCorrect: false,
};

const dictationQuestion = {
  id: 6,
  position: 6,
  type: 'DICTATION',
  audioText: 'How are you today?',
  answered: false,
  lastAnswerCorrect: false,
};

describe('QuestionCard', () => {
  it('renders translation question with input', () => {
    render(<QuestionCard question={translationQuestion} index={1} />);
    expect(screen.getByText('Pregunta 1 · Traducción')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Escribe tu respuesta...')).toBeInTheDocument();
    expect(screen.getByText('Comprobar respuesta')).toBeInTheDocument();
    expect(screen.getByText('Ver pista')).toBeInTheDocument();
  });

  it('renders multiple choice question with options', () => {
    render(<QuestionCard question={mcQuestion} index={2} />);
    expect(screen.getByText('Pregunta 2 · Opción múltiple')).toBeInTheDocument();
    expect(screen.getByText('Hola')).toBeInTheDocument();
    expect(screen.getByText('Adiós')).toBeInTheDocument();
    expect(screen.getByText('Gracias')).toBeInTheDocument();
  });

  it('renders listening question with audio button and input', () => {
    render(<QuestionCard question={listeningQuestion} index={3} />);
    expect(screen.getByText('Pregunta 3 · Comprensión auditiva')).toBeInTheDocument();
    expect(screen.getByText('Escuchar')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Escribe tu respuesta...')).toBeInTheDocument();
  });

  it('renders word order question with available words', () => {
    render(<QuestionCard question={wordOrderQuestion} index={4} />);
    expect(screen.getByText('Pregunta 4 · Ordena las palabras')).toBeInTheDocument();
    expect(screen.getByText('Toca las palabras en el orden correcto')).toBeInTheDocument();
    expect(screen.getByText('student')).toBeInTheDocument();
    expect(screen.getByText('a')).toBeInTheDocument();
    expect(screen.getByText('I')).toBeInTheDocument();
    expect(screen.getByText('am')).toBeInTheDocument();
    expect(screen.getByText('Comprobar respuesta')).toBeInTheDocument();
  });

  it('renders matching question with two columns', () => {
    render(<QuestionCard question={matchingQuestion} index={5} />);
    expect(screen.getByText('Pregunta 5 · Empareja')).toBeInTheDocument();
    expect(screen.getByText('hello')).toBeInTheDocument();
    expect(screen.getByText('goodbye')).toBeInTheDocument();
    expect(screen.getByText('hola')).toBeInTheDocument();
    expect(screen.getByText('adiós')).toBeInTheDocument();
    expect(screen.getByText('Comprobar respuesta')).toBeInTheDocument();
  });

  it('renders dictation question with audio button and no prompt', () => {
    render(<QuestionCard question={dictationQuestion} index={6} />);
    expect(screen.getByText('Pregunta 6 · Dictado')).toBeInTheDocument();
    expect(screen.getByText('Escuchar y escribir')).toBeInTheDocument();
    expect(screen.getByText(/Escucha el audio y escribe lo que oyes/)).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Escribe tu respuesta...')).toBeInTheDocument();
  });

  it('shows answered state', () => {
    const answered = { ...translationQuestion, answered: true, lastAnswerCorrect: true };
    render(<QuestionCard question={answered} index={1} />);
    expect(screen.getByText('Resuelto')).toBeInTheDocument();
  });

  it('shows hint when hint button is clicked', async () => {
    const user = (await import('@testing-library/user-event')).default;
    render(<QuestionCard question={translationQuestion} index={1} />);
    const hintBtn = screen.getByText('Ver pista');
    await user.click(hintBtn);
    expect(screen.getByText(/It is a greeting/)).toBeInTheDocument();
  });
});
