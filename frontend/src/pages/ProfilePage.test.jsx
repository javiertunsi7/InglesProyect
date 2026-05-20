import { render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import ProfilePage from './ProfilePage.jsx';

vi.mock('../services/userService.js', () => ({
  userService: {
    getProfile: vi.fn().mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      displayName: 'Test User',
      initials: 'TU',
      bio: 'Aprender ingés.',
      avatarUrl: null,
      role: 'USER',
      currentStreak: 7,
      totalXp: 2500,
      dailyGoalMinutes: 15,
      dailyGoalXp: 50,
      createdAt: '2025-01-01T00:00:00Z',
    }),
  },
}));

describe('ProfilePage', () => {
  it('renders user info from the API', async () => {
    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Test User')).toBeInTheDocument();
    });
    expect(screen.getByText('test@example.com')).toBeInTheDocument();
    expect(screen.getByText('Aprender ingés.')).toBeInTheDocument();
    expect(screen.getByText('TU')).toBeInTheDocument();
  });

  it('renders stats cards', async () => {
    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('7')).toBeInTheDocument();
      expect(screen.getByText(/2500/)).toBeInTheDocument();
    });
    expect(screen.getByText('Racha actual')).toBeInTheDocument();
    expect(screen.getByText('XP totales')).toBeInTheDocument();
    expect(screen.getByText('Meta diaria (min)')).toBeInTheDocument();
    expect(screen.getByText('Meta diaria (XP)')).toBeInTheDocument();
  });

  it('shows link to settings', async () => {
    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Ajustes')).toBeInTheDocument();
    });
  });

  it('shows loading state initially', () => {
    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>,
    );

    expect(screen.getByText('Cargando perfil...')).toBeInTheDocument();
  });
});
