import apiClient from '../api/apiClient.js';

export const categoryService = {
  getAll: async () => {
    const response = await apiClient.get('/categories');
    return response.data;
  },
  getByType: async (categoryType) => {
    const response = await apiClient.get(`/categories/${categoryType}`);
    return response.data;
  },
};
