import api from '../api/apiClient.js';

export const questService = {
  getToday() {
    return api.get('/quests/today').then((r) => r.data);
  },
};
