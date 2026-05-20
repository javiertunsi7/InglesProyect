import axios from 'axios';

const TOKEN_STORAGE_KEY = 'english-learning.token';
const USER_STORAGE_KEY = 'english-learning.user';

const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      localStorage.removeItem(USER_STORAGE_KEY);
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
      return Promise.reject(
        new Error('Tu sesión ha expirado. Vuelve a iniciar sesión.'),
      );
    }
    const message =
      error.response?.data?.message ||
      'No se pudo conectar con el servidor. Inténtalo más tarde.';
    return Promise.reject(new Error(message));
  },
);

export { TOKEN_STORAGE_KEY, USER_STORAGE_KEY };
export default apiClient;
