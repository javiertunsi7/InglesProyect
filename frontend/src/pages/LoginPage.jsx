import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from || '/';

  const [email, setEmail] = useState('demo@english.local');
  const [password, setPassword] = useState('demo1234');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await login(email, password);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="page page--auth">
      <article className="auth-card">
        <p className="eyebrow">Acceso</p>
        <h1 className="section-title">Bienvenido de vuelta.</h1>
        <p className="muted" style={{ marginTop: '0.25rem' }}>
          Entra para sincronizar tu racha y guardar tu progreso.
        </p>

        <form onSubmit={handleSubmit} className="auth-form">
          <label className="auth-form__field">
            <span>Correo electrónico</span>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              autoComplete="email"
            />
          </label>
          <label className="auth-form__field">
            <span>Contraseña</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              autoComplete="current-password"
              minLength={8}
            />
          </label>

          {error && <p className="alert alert--error">{error}</p>}

          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? 'Entrando...' : 'Entrar'}
          </button>

          <Link to="/recuperar" className="muted" style={{ fontSize: '0.82rem', textAlign: 'center', display: 'block', marginTop: '0.25rem' }}>
            ¿Olvidaste tu contraseña?
          </Link>
        </form>

        <p className="muted" style={{ marginTop: '1.25rem', fontSize: '0.85rem' }}>
          ¿No tienes cuenta? <Link to="/registro" style={{ color: 'var(--accent)' }}>Regístrate</Link>
        </p>
        <p className="muted" style={{ fontSize: '0.78rem' }}>
          Demo: <span className="mono">demo@english.local / demo1234</span>
        </p>
      </article>
    </section>
  );
}

export default LoginPage;
