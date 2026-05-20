import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import DictionaryPage from './DictionaryPage.jsx';

vi.mock('../services/dictionaryService.js', () => ({
  dictionaryService: {
    search: vi.fn().mockResolvedValue({
      items: [
        { id: 1, word: 'hello', phonetic: '/heˈloʊ/', partOfSpeech: 'interjection', definitionEs: 'Hola', exampleEn: 'Hello!', exampleEs: '¡Hola!', categoryType: 'GENERAL', levelCode: 'A1' },
        { id: 2, word: 'bug', phonetic: '/bʌɡ/', partOfSpeech: 'noun', definitionEs: 'Error', exampleEn: 'I fixed a bug.', exampleEs: 'Arreglé un bug.', categoryType: 'TECH', levelCode: 'A1' },
      ],
      total: 2,
      page: 0,
      size: 30,
      totalPages: 1,
    }),
  },
}));

function renderPage() {
  return render(
    <BrowserRouter>
      <DictionaryPage />
    </BrowserRouter>,
  );
}

describe('DictionaryPage', () => {
  it('renders search input and filter chips', async () => {
    renderPage();
    expect(await screen.findByPlaceholderText('Buscar palabra o definición...')).toBeInTheDocument();
    expect(screen.getByText('Todas')).toBeInTheDocument();
    expect(screen.getByText('General')).toBeInTheDocument();
    expect(screen.getByText('Técnico')).toBeInTheDocument();
  });

  it('renders dictionary entries', async () => {
    renderPage();
    expect(await screen.findByText('hello')).toBeInTheDocument();
    expect(screen.getByText('bug')).toBeInTheDocument();
  });

  it('renders escuchar buttons', async () => {
    renderPage();
    const buttons = await screen.findAllByText('Escuchar');
    expect(buttons.length).toBe(2);
  });

  it('shows entry count', async () => {
    renderPage();
    expect(await screen.findByText('2 entradas')).toBeInTheDocument();
  });
});
