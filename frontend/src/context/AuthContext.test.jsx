import { act, renderHook, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from './AuthContext.jsx';

vi.mock('../services/authService.js', () => ({
  authService: {
    login: vi.fn(async (email) => ({
      token: 'fake-jwt',
      expiresInSeconds: 3600,
      userId: 1,
      email,
      displayName: 'Test User',
      role: 'USER',
    })),
    register: vi.fn(),
  },
}));

const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;

describe('AuthContext', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  it('treats the user as unauthenticated when no token is stored', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it('discards a stale profile without a matching token', () => {
    localStorage.setItem(
      'english-learning.user',
      JSON.stringify({ id: 1, email: 'a@b.c', displayName: 'Ghost' }),
    );
    // No token in storage → AuthContext must NOT treat us as logged in.
    const { result } = renderHook(() => useAuth(), { wrapper });
    expect(result.current.isAuthenticated).toBe(false);
    expect(localStorage.getItem('english-learning.user')).toBeNull();
  });

  it('persists session on login and clears it on logout', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(() => result.current.login('demo@english.local', 'demo1234'));

    await waitFor(() => {
      expect(result.current.isAuthenticated).toBe(true);
    });
    expect(localStorage.getItem('english-learning.token')).toBe('fake-jwt');

    act(() => result.current.logout());

    expect(result.current.isAuthenticated).toBe(false);
    expect(localStorage.getItem('english-learning.token')).toBeNull();
    expect(localStorage.getItem('english-learning.user')).toBeNull();
  });

  it('reacts to the auth:unauthorized event by clearing session', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    await act(() => result.current.login('demo@english.local', 'demo1234'));
    expect(result.current.isAuthenticated).toBe(true);

    act(() => window.dispatchEvent(new CustomEvent('auth:unauthorized')));

    await waitFor(() => {
      expect(result.current.isAuthenticated).toBe(false);
    });
  });
});
