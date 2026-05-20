import apiClient from '../api/apiClient.js';

export const leaderboardService = {
  getLeaderboard: async () => {
    const response = await apiClient.get('/leaderboard');
    return response.data;
  },
};