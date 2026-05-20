import { useState } from 'react';
import { createCheckoutSession } from '../services/subscriptionService';
import { useAuth } from '../context/AuthContext';
import '../styles/pricing.css';

const PLANS = [
  {
    id: 'monthly',
    priceId: import.meta.env.VITE_STRIPE_PRICE_MONTHLY || 'price_monthly_placeholder',
    price: '7,99',
    label: 'Mensual',
    features: [
      'Acceso a niveles C1 y C2',
      'Todos los tipos de ejercicio',
      'Seguimiento de progreso completo',
      'Sin anuncios',
    ],
  },
  {
    id: 'yearly',
    priceId: import.meta.env.VITE_STRIPE_PRICE_YEARLY || 'price_yearly_placeholder',
    price: '69,99',
    label: 'Anual',
    badge: 'Ahorra 26€',
    features: [
      'Todo lo del plan Mensual',
      'Precio reducido: 5,83€/mes',
      'Acceso prioritario a nuevas funcionalidades',
    ],
  },
];

export default function PricingPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(null);
  const [error, setError] = useState('');

  const handleSelect = async (plan) => {
    if (!user) {
      window.location.href = '/login';
      return;
    }
    setLoading(plan.id);
    setError('');
    try {
      const { url } = await createCheckoutSession(plan.priceId);
      window.location.href = url;
    } catch (err) {
      setError('Error al crear la sesión de pago. Inténtalo de nuevo.');
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="pricing">
      <div className="pricing__header">
        <h1>Desbloquea todo tu potencial</h1>
        <p>
          Los niveles C1 y C2 requieren una suscripción Premium.
          Elige el plan que mejor se adapte a ti.
        </p>
      </div>

      <div className="pricing__plans">
        {PLANS.map((plan) => (
          <div key={plan.id} className={`pricing__card ${plan.badge ? 'pricing__card--featured' : ''}`}>
            {plan.badge && <span className="pricing__badge">{plan.badge}</span>}
            <h2 className="pricing__plan-label">{plan.label}</h2>
            <div className="pricing__price">
              <span className="pricing__amount">{plan.price}</span>
              <span className="pricing__period">€/{plan.id === 'monthly' ? 'mes' : 'año'}</span>
            </div>
            <ul className="pricing__features">
              {plan.features.map((f) => (
                <li key={f} className="pricing__feature">✓ {f}</li>
              ))}
            </ul>
            <button
              className={`btn pricing__btn ${loading === plan.id ? 'btn--loading' : ''}`}
              onClick={() => handleSelect(plan)}
              disabled={loading !== null}
            >
              {loading === plan.id ? 'Redirigiendo...' : 'Empezar prueba gratis'}
            </button>
          </div>
        ))}
      </div>

      {error && <p className="pricing__error">{error}</p>}

      <p className="pricing__footnote">
        Cancelación gratuita en cualquier momento. Periodo de prueba de 30 días.
        Pago único seguro vía Stripe.
      </p>
    </div>
  );
}
