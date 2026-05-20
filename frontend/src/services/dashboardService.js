import apiClient from '../api/apiClient.js';

export const dashboardService = {
  getDashboard: async () => {
    const response = await apiClient.get('/dashboard');
    return response.data;
  },
  getMe: async () => {
    const response = await apiClient.get('/me');
    return response.status === 204 ? null : response.data;
  },
  getWordOfDay: async () => {
    const response = await apiClient.get('/words/today');
    return response.status === 204 ? null : response.data;
  },
};
