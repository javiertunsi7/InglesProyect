import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

const manifest = {
  name: 'enclave · Inglés CEFR',
  short_name: 'enclave',
  description: 'Aprende inglés con ejercicios basados en el Marco Común Europeo',
  theme_color: '#0d0710',
  background_color: '#0d0710',
  display: 'standalone',
  start_url: '/',
  lang: 'es',
};

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.svg', 'pwa-64x64.png', 'pwa-192x192.png', 'pwa-512x512.png', 'apple-icon-180.png'],
      manifest,
      strategies: 'injectManifest',
      srcDir: 'src',
      filename: 'sw.js',
      injectManifest: {
        globPatterns: ['**/*.{js,css,html,svg,png,ico,woff2}'],
      },
    }),
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.js'],
    css: false,
  },
});
