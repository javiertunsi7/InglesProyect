import api from '../api/apiClient.js';

export const statsService = {
  getStats() {
    return api.get('/progress/stats').then((r) => r.data);
  },
};
