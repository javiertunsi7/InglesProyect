import apiClient from '../api/apiClient.js';

export const levelService = {
  getByCategory: async (categoryType) => {
    const response = await apiClient.get(`/categories/${categoryType}/levels`);
    return response.data;
  },
  getDetail: async (categoryType, levelCode) => {
    const response = await apiClient.get(`/categories/${categoryType}/levels/${levelCode}`);
    return response.data;
  },
};
