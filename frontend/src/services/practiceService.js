import apiClient from '../api/apiClient.js';

export const practiceService = {
  getDaily: async () => {
    const response = await apiClient.get('/practice/daily');
    return response.data;
  },
};
