import apiClient from '../api/apiClient.js';

export const dictionaryService = {
  /**
   * Busca entradas de diccionario en el backend.
   * @param {{q?: string, category?: 'GENERAL'|'TECH', level?: 'A1'|'A2'|'B1'|'B2'|'C1'|'C2', page?: number, size?: number}} filters
   */
  search: async (filters = {}) => {
    const params = {};
    if (filters.q && filters.q.trim()) params.q = filters.q.trim();
    if (filters.category) params.category = filters.category;
    if (filters.level) params.level = filters.level;
    if (filters.page !== undefined) params.page = filters.page;
    if (filters.size !== undefined) params.size = filters.size;
    const response = await apiClient.get('/dictionary', { params });
    return response.data;
  },
};
