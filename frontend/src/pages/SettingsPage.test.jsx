import { render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import SettingsPage from './SettingsPage.jsx';

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
    updateProfile: vi.fn().mockResolvedValue({}),
    changePassword: vi.fn().mockResolvedValue(undefined),
  },
}));

describe('SettingsPage', () => {
  it('renders profile form after loading', async () => {
    render(
      <MemoryRouter>
        <SettingsPage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByDisplayValue('Test User')).toBeInTheDocument();
    });
    expect(screen.getByDisplayValue('Aprender ingés.')).toBeInTheDocument();
    expect(screen.getByDisplayValue('15')).toBeInTheDocument();
    expect(screen.getByDisplayValue('50')).toBeInTheDocument();
  });

  it('renders password form', async () => {
    render(
      <MemoryRouter>
        <SettingsPage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Cambiar contraseña' })).toBeInTheDocument();
    });
    expect(screen.getByText('Contraseña actual')).toBeInTheDocument();
    expect(screen.getByText('Nueva contraseña')).toBeInTheDocument();
    expect(screen.getByText('Confirmar contraseña')).toBeInTheDocument();
  });
});
