import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

/**
 * Envuelve rutas que sólo tienen sentido con sesión iniciada.
 * Si no hay usuario, redirige a /login conservando la URL original en
 * {@code location.state.from} para que LoginPage pueda volver tras el login.
 */
function RequireAuth({ children }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return children;
}

export default RequireAuth;
