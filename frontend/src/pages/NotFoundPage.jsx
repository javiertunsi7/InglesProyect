import { Link } from 'react-router-dom';

function NotFoundPage() {
  return (
    <section className="page">
      <p className="eyebrow">404</p>
      <h1 className="display">Página no encontrada.</h1>
      <p className="lead" style={{ marginTop: '1rem' }}>
        La ruta que buscas no existe o ya no está disponible.
      </p>
      <p style={{ marginTop: '1.5rem' }}>
        <Link to="/" className="btn btn--primary">Volver al inicio</Link>
      </p>
    </section>
  );
}

export default NotFoundPage;
