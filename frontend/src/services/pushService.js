import apiClient from '../api/apiClient.js';

function arrayBufferToBase64(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

export const pushService = {
  getVapidPublicKey: async () => {
    const response = await apiClient.get('/push/vapid-public-key');
    return response.data.publicKey;
  },
  subscribe: async (subscription) => {
    const data = {
      endpoint: subscription.endpoint,
      keys: {
        p256dh: arrayBufferToBase64(subscription.getKey('p256dh')),
        auth: arrayBufferToBase64(subscription.getKey('auth')),
      },
    };
    await apiClient.post('/push/subscribe', data);
  },
  unsubscribe: async (endpoint) => {
    await apiClient.delete('/push/unsubscribe', { data: { endpoint } });
  },
  sendTest: async () => {
    await apiClient.post('/push/test');
  },
};
