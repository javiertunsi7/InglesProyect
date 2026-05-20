import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import HomePage from './HomePage.jsx';

vi.mock('../services/dashboardService.js', () => ({
  dashboardService: {
    getDashboard: vi.fn().mockResolvedValue({
      greeting: { salutation: 'Hola', displayName: 'Test', headline: 'Hoy practicas.', subhead: 'Sigue así', estimatedMinutes: 8 },
      continueCard: null,
      tracks: [{ id: 1, type: 'GENERAL', displayName: 'General', description: 'Test', totalLevels: 6, progressPercent: 30 }],
      streak: { currentStreak: 5, longestStreak: 10, totalXp: 500, week: [{ dayLabel: 'L', active: true, today: false }, { dayLabel: 'M', active: true, today: true }] },
      dailyGoal: { minutesPracticed: 5, dailyGoalMinutes: 10, xpEarned: 50, dailyGoalXp: 100, progressPercent: 50, minutesRemaining: 5 },
      wordOfDay: { word: 'deploy', phonetic: '/dɪˈplɔɪ/', partOfSpeech: 'verb', definitionEs: 'Desplegar', exampleEn: 'We deploy.', exampleEs: 'Desplegamos.' },
    }),
  },
}));

function renderPage() {
  return render(
    <BrowserRouter>
      <HomePage />
    </BrowserRouter>,
  );
}

describe('HomePage', () => {
  it('renders greeting and headline', async () => {
    renderPage();
    expect(await screen.findByText((c) => c.includes('Hola'))).toBeInTheDocument();
    expect(screen.getByText((c) => c.includes('Hoy practicas'))).toBeInTheDocument();
  });

  it('renders streak info', async () => {
    renderPage();
    expect(await screen.findByText('5')).toBeInTheDocument();
  });

  it('renders word of the day', async () => {
    renderPage();
    expect(await screen.findByText('deploy')).toBeInTheDocument();
    expect(await screen.findByText('Escuchar')).toBeInTheDocument();
  });

  it('renders daily goal', async () => {
    renderPage();
    expect(await screen.findByText('Meta diaria')).toBeInTheDocument();
  });

  it('renders track cards', async () => {
    renderPage();
    expect(await screen.findByText('General')).toBeInTheDocument();
  });
});
