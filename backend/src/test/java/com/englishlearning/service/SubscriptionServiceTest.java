package com.englishlearning.service;

import com.englishlearning.domain.model.UserSubscription;
import com.englishlearning.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de la lógica que distingue usuarios premium del resto. La parte de
 * persistencia (createOrUpdate) la cubre la integración con Stripe en el
 * controller; aquí nos centramos en isPremium y getStatus.
 */
class SubscriptionServiceTest {

    private UserSubscriptionRepository repository;
    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserSubscriptionRepository.class);
        service = new SubscriptionService(repository);
    }

    @Test
    @DisplayName("Sin suscripción → no premium")
    void noSubscription() {
        when(repository.findByUserId(any())).thenReturn(Optional.empty());

        assertFalse(service.isPremium(42L));
        assertFalse(service.getStatus(42L).isPremium());
    }

    @Test
    @DisplayName("status=active → premium")
    void activeSubscription() {
        when(repository.findByUserId(eq(42L)))
                .thenReturn(Optional.of(subWith("active")));

        assertTrue(service.isPremium(42L));
        var status = service.getStatus(42L);
        assertTrue(status.isPremium());
        assertEquals("monthly", status.getPlan());
    }

    @Test
    @DisplayName("status=trialing → premium (incluye el periodo de prueba)")
    void trialingSubscription() {
        when(repository.findByUserId(eq(42L)))
                .thenReturn(Optional.of(subWith("trialing")));

        assertTrue(service.isPremium(42L));
    }

    @Test
    @DisplayName("status=canceled → NO premium")
    void canceledSubscription() {
        when(repository.findByUserId(eq(42L)))
                .thenReturn(Optional.of(subWith("canceled")));

        assertFalse(service.isPremium(42L));
        // getStatus sigue devolviendo el objeto, pero con premium=false.
        var status = service.getStatus(42L);
        assertFalse(status.isPremium());
        assertEquals("canceled", status.getStatus());
    }

    @Test
    @DisplayName("status=incomplete (pago en curso o fallido) → NO premium")
    void incompleteSubscription() {
        when(repository.findByUserId(eq(42L)))
                .thenReturn(Optional.of(subWith("incomplete")));

        assertFalse(service.isPremium(42L));
    }

    private UserSubscription subWith(String status) {
        return UserSubscription.builder()
                .userId(42L)
                .stripeCustomerId("cus_42")
                .stripeSubscriptionId("sub_42")
                .plan("monthly")
                .status(status)
                .cancelAtPeriodEnd(false)
                .build();
    }
}
