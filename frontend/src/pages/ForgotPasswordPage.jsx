import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authService } from '../services/authService.js';

function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await authService.forgotPassword(email);
      setSent(true);
    } catch (err) {
      setError(err?.response?.data?.message || err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (sent) {
    return (
      <section className="page page--auth">
        <article className="auth-card" style={{ textAlign: 'center' }}>
          <h1 className="section-title">Revisa tu correo</h1>
          <p className="muted" style={{ marginTop: '0.5rem' }}>
            Si existe una cuenta con ese email, recibirás un enlace para restablecer tu contraseña.
          </p>
          <Link to="/login" className="btn btn--ghost" style={{ marginTop: '1.25rem', display: 'inline-block' }}>
            Volver al inicio de sesión
          </Link>
        </article>
      </section>
    );
  }

  return (
    <section className="page page--auth">
      <article className="auth-card">
        <p className="eyebrow">Recuperar acceso</p>
        <h1 className="section-title">¿Olvidaste tu contraseña?</h1>
        <p className="muted" style={{ marginTop: '0.25rem' }}>
          Te enviaremos un enlace para crear una nueva.
        </p>

        <form onSubmit={handleSubmit} className="auth-form">
          <label className="auth-form__field">
            <span>Correo electrónico</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </label>

          {error && <p className="alert alert--error">{error}</p>}

          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? 'Enviando...' : 'Enviar enlace'}
          </button>
        </form>

        <p className="muted" style={{ marginTop: '1.25rem', fontSize: '0.85rem' }}>
          <Link to="/login" style={{ color: 'var(--accent)' }}>Volver al inicio de sesión</Link>
        </p>
      </article>
    </section>
  );
}

export default ForgotPasswordPage;
