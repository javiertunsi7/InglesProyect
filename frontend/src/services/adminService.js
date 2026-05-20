import apiClient from '../api/apiClient.js';

export const adminService = {
  getUsers: async () => {
    const response = await apiClient.get('/admin/users');
    return response.data;
  },
  getStats: async () => {
    const response = await apiClient.get('/admin/stats');
    return response.data;
  },
  getExercises: async () => {
    const response = await apiClient.get('/admin/exercises');
    return response.data;
  },
  createExercise: async (data) => {
    const response = await apiClient.post('/admin/exercises', data);
    return response.data;
  },
  updateExercise: async (id, data) => {
    const response = await apiClient.put(`/admin/exercises/${id}`, data);
    return response.data;
  },
  deleteExercise: async (id) => {
    await apiClient.delete(`/admin/exercises/${id}`);
  },
  getDictionary: async () => {
    const response = await apiClient.get('/admin/dictionary');
    return response.data;
  },
  createDictionaryEntry: async (data) => {
    const response = await apiClient.post('/admin/dictionary', data);
    return response.data;
  },
  updateDictionaryEntry: async (id, data) => {
    const response = await apiClient.put(`/admin/dictionary/${id}`, data);
    return response.data;
  },
  deleteDictionaryEntry: async (id) => {
    await apiClient.delete(`/admin/dictionary/${id}`);
  },
  getWordsOfDay: async () => {
    const response = await apiClient.get('/admin/words');
    return response.data;
  },
  createWordOfDay: async (data) => {
    const response = await apiClient.post('/admin/words', data);
    return response.data;
  },
  updateWordOfDay: async (id, data) => {
    const response = await apiClient.put(`/admin/words/${id}`, data);
    return response.data;
  },
  deleteWordOfDay: async (id) => {
    await apiClient.delete(`/admin/words/${id}`);
  },
};