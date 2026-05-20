import '@testing-library/jest-dom/vitest';
import { afterEach, vi } from 'vitest';
import { cleanup } from '@testing-library/react';

if (typeof Notification === 'undefined') {
  globalThis.Notification = { permission: 'default', requestPermission: vi.fn() };
}

afterEach(() => {
  cleanup();
  localStorage.clear();
});
