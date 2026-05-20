import { Component } from 'react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="state state--error" style={{ padding: '4rem 1rem', textAlign: 'center' }}>
          <h2 style={{ fontFamily: 'var(--font-display)', fontSize: '1.5rem' }}>
            Algo salió mal
          </h2>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>
            Ocurrió un error inesperado. Intenta recargar la página.
          </p>
          <button
            type="button"
            className="btn btn--primary"
            style={{ marginTop: '1rem' }}
            onClick={() => window.location.reload()}
          >
            Recargar página
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

export default ErrorBoundary;
