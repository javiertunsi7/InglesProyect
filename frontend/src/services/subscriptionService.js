import api from '../api/apiClient.js';

export const getSubscriptionStatus = async () => {
  const { data } = await api.get('/subscription/me');
  return data;
};

export const createCheckoutSession = async (priceId) => {
  const { data } = await api.post('/stripe/create-checkout-session', { priceId });
  return data;
};

export const createPortalSession = async () => {
  const { data } = await api.post('/stripe/create-portal-session');
  return data;
};
