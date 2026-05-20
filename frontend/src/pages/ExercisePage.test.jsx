import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import ExercisePage from './ExercisePage.jsx';

vi.mock('../services/exerciseService.js', () => ({
  exerciseService: {
    getDetail: vi.fn().mockResolvedValue({
      id: 1,
      categoryType: 'GENERAL',
      levelCode: 'A1',
      levelName: 'Principiante',
      position: 1,
      title: 'Test',
      topic: 'Greetings',
      questionsCount: 2,
      estimatedMinutes: 5,
      xpReward: 10,
      status: 'AVAILABLE',
      stars: 0,
      correctAnswers: 0,
      totalAnswered: 0,
      questions: [
        { id: 1, position: 1, type: 'TRANSLATION', prompt: 'Translate "hello"', hint: 'Greeting', answered: false, lastAnswerCorrect: false },
        { id: 2, position: 2, type: 'MULTIPLE_CHOICE', prompt: 'Meaning of "hello"', options: [{ id: 1, label: 'A', value: 'Hola' }, { id: 2, label: 'B', value: 'Adiós' }], answered: false, lastAnswerCorrect: false },
      ],
    }),
    submitAnswer: vi.fn(),
  },
}));

function renderPage(path = '/GENERAL/A1/1') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/:categoryType/:levelCode/:exerciseId" element={<ExercisePage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('ExercisePage', () => {
  it('renders exercise title and questions', async () => {
    renderPage();
    expect(await screen.findByText('Ejercicio 1.')).toBeInTheDocument();
    expect(screen.getByText('Translate "hello"')).toBeInTheDocument();
    expect(screen.getByText('Meaning of "hello"')).toBeInTheDocument();
  });

  it('shows question count and XP reward', async () => {
    renderPage();
    expect(await screen.findByText('2 preguntas')).toBeInTheDocument();
    expect(screen.getByText('10 XP')).toBeInTheDocument();
  });

  it('shows progress bar stats', async () => {
    const { container } = renderPage();
    await screen.findByText('Ejercicio 1.');
    expect(container.querySelector('.exercise-progress-bar')).toBeInTheDocument();
    expect(container.querySelector('.exercise-progress-bar__label')).toBeInTheDocument();
  });
});
