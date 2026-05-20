import { createContext, useContext, useCallback, useEffect, useMemo, useState } from 'react';
import { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from '../api/apiClient.js';
import { authService } from '../services/authService.js';
import { userService } from '../services/userService.js';

const AuthContext = createContext(null);

/**
 * Reads the persisted session only if BOTH the user profile and the JWT are
 * present. Avoids the "ghost user" scenario where the token was cleared on a
 * 401 but the profile lingered in localStorage and made the app think the
 * user was still authenticated.
 */
function readStoredUser() {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  const raw = localStorage.getItem(USER_STORAGE_KEY);
  if (!token || !raw) {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  useEffect(() => {
    const handleUnauthorized = () => {
      localStorage.removeItem(USER_STORAGE_KEY);
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      setUser(null);
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  const persistSession = (auth) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, auth.token);
    const profile = {
      id: auth.userId,
      email: auth.email,
      displayName: auth.displayName,
      role: auth.role,
    };
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(profile));
    setUser(profile);
  };

  const login = async (email, password) => {
    const auth = await authService.login(email, password);
    persistSession(auth);
  };

  const register = async (email, password, displayName) => {
    const auth = await authService.register(email, password, displayName);
    persistSession(auth);
  };

  const refreshProfile = useCallback(async () => {
    try {
      const profile = await userService.getProfile();
      const stored = {
        id: profile.id,
        email: profile.email,
        displayName: profile.displayName,
        role: profile.role,
      };
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(stored));
      setUser(stored);
      return profile;
    } catch {
      return null;
    }
  }, []);

  const logout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    setUser(null);
  };

  const value = useMemo(
    () => ({ user, isAuthenticated: Boolean(user), login, register, logout, refreshProfile }),
    [user, refreshProfile],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
}
