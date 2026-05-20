import apiClient from '../api/apiClient.js';

export const progressService = {
  getOverview: async () => {
    const response = await apiClient.get('/progress');
    return response.data;
  },
};
