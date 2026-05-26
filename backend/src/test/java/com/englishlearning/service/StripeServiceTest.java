package com.englishlearning.service;

import com.englishlearning.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests para el portero de {@link StripeService#createCheckoutSession}.
 *
 * <p>Sólo cubre la lógica que NO depende del SDK Stripe (la whitelist de
 * priceId). El happy path llama a {@code Session.create} de Stripe, que es
 * estático y requeriría mockito-inline o sustitución del SDK; queda fuera.
 *
 * <p>El objetivo del test es proteger el agujero de seguridad: el cliente NO
 * decide qué pagar.
 */
class StripeServiceTest {

    private static final String MONTHLY = "price_monthly_real";
    private static final String YEARLY  = "price_yearly_real";

    private StripeService service;

    @BeforeEach
    void setUp() {
        service = new StripeService();
        // Los campos vienen de @Value; en test los seteamos directamente.
        ReflectionTestUtils.setField(service, "monthlyPriceId", MONTHLY);
        ReflectionTestUtils.setField(service, "yearlyPriceId", YEARLY);
        ReflectionTestUtils.setField(service, "webhookSecret", "whsec_test");
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:5173");
    }

    @Test
    @DisplayName("priceId nulo → BadRequestException")
    void rejectsNullPriceId() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.createCheckoutSession(1L, null, "demo@english.local"));
        assertEquals("priceId no autorizado.", ex.getMessage());
    }

    @Test
    @DisplayName("priceId arbitrario fuera de la whitelist → BadRequestException")
    void rejectsArbitraryPriceId() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.createCheckoutSession(1L, "price_free_for_attackers", "demo@english.local"));
        assertEquals("priceId no autorizado.", ex.getMessage());
    }

    @Test
    @DisplayName("priceId mensual válido pasa el portero (no testeamos la llamada a Stripe)")
    void monthlyIsWhitelisted() {
        // Si llega a Session.create estamos fuera del scope del test — Stripe
        // SDK lanzará al no haber Stripe.apiKey configurado. Aceptamos cualquier
        // RuntimeException distinta de BadRequestException como "pasó el portero".
        try {
            service.createCheckoutSession(1L, MONTHLY, "demo@english.local");
        } catch (BadRequestException e) {
            throw new AssertionError("El priceId mensual fue rechazado por la whitelist", e);
        } catch (Exception ignored) {
            // OK: el SDK falla más adelante. Lo que nos importa es que NO
            // ha sido la BadRequestException la que ha cortado.
        }
    }

    @Test
    @DisplayName("priceId anual válido pasa el portero")
    void yearlyIsWhitelisted() {
        try {
            service.createCheckoutSession(1L, YEARLY, "demo@english.local");
        } catch (BadRequestException e) {
            throw new AssertionError("El priceId anual fue rechazado por la whitelist", e);
        } catch (Exception ignored) {
            // idem que el test anterior.
        }
    }
}
