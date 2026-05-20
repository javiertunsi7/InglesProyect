import apiClient from '../api/apiClient.js';

export const exerciseService = {
  getDetail: async (exerciseId) => {
    const response = await apiClient.get(`/exercises/${exerciseId}`);
    return response.data;
  },
  submitAnswer: async (questionId, answer) => {
    const response = await apiClient.post(`/questions/${questionId}/answer`, { answer });
    return response.data;
  },
};
