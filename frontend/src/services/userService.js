import api from '../api/apiClient.js';

export const userService = {
  getProfile() {
    return api.get('/users/me/profile').then((r) => r.data);
  },

  updateProfile(data) {
    return api.put('/users/me/profile', data).then((r) => r.data);
  },

  changePassword(currentPassword, newPassword) {
    return api.put('/users/me/password', { currentPassword, newPassword });
  },
};
