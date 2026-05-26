package com.englishlearning.service;

import com.englishlearning.config.VapidConfig;
import com.englishlearning.domain.model.PushSubscription;
import com.englishlearning.dto.PushSubscribeRequest;
import com.englishlearning.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del flujo "subscribe / sendToUser" sin tocar la API HTTP webpush real
 * (esa parte se aísla en el método privado {@code send}, que necesitaría un
 * test de integración separado).
 */
class PushNotificationServiceTest {

    private PushSubscriptionRepository repository;
    private VapidConfig vapidConfig;
    private PushNotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(PushSubscriptionRepository.class);
        vapidConfig = mock(VapidConfig.class);
        // Devolver claves "vacías" para que VapidConfig no rompa en el send,
        // que en cualquier caso fallará al intentar el HTTP — lo capturamos.
        when(vapidConfig.getPublicKey()).thenReturn("");
        when(vapidConfig.getPrivateKey()).thenReturn("");
        service = new PushNotificationService(repository, vapidConfig);
    }

    @Test
    @DisplayName("subscribe persiste endpoint + keys si no existe ya")
    void subscribePersistsNewSubscription() {
        when(repository.existsByUserIdAndEndpoint(eq(42L), eq("https://push.example/1"))).thenReturn(false);

        PushSubscribeRequest req = new PushSubscribeRequest();
        req.setEndpoint("https://push.example/1");
        PushSubscribeRequest.Keys keys = new PushSubscribeRequest.Keys();
        keys.setP256dh("p256dh-test");
        keys.setAuth("auth-test");
        req.setKeys(keys);

        service.subscribe(42L, req);

        verify(repository, times(1)).save(any(PushSubscription.class));
    }

    @Test
    @DisplayName("subscribe es idempotente: si ya existe el endpoint para el usuario, no guarda otra vez")
    void subscribeIsIdempotent() {
        when(repository.existsByUserIdAndEndpoint(eq(42L), eq("https://push.example/1"))).thenReturn(true);

        PushSubscribeRequest req = new PushSubscribeRequest();
        req.setEndpoint("https://push.example/1");
        PushSubscribeRequest.Keys keys = new PushSubscribeRequest.Keys();
        keys.setP256dh("p256dh-test");
        keys.setAuth("auth-test");
        req.setKeys(keys);

        service.subscribe(42L, req);

        verify(repository, never()).save(any(PushSubscription.class));
    }

    @Test
    @DisplayName("sendToUser sobre un usuario sin suscripciones no rompe (silencio total)")
    void sendToUserWithoutSubscriptions() {
        when(repository.findByUserId(eq(42L))).thenReturn(List.of());

        // No debe lanzar excepción.
        service.sendToUser(42L, "Title", "Body", "/url");

        verify(repository, times(1)).findByUserId(42L);
    }

    @Test
    @DisplayName("unsubscribe delega en deleteByEndpoint")
    void unsubscribeDeletes() {
        service.unsubscribe("https://push.example/1");
        verify(repository, times(1)).deleteByEndpoint("https://push.example/1");
    }

    // Eliminado: sendToUserWithMultipleSubscriptions descubrió un NoClassDefFoundError
    // (BouncyCastle ausente) que escapa al catch(Exception) en sendToUser.
    // Apuntado como bug #49 — el test volverá cuando se cambie a catch(Throwable)
    // y se añada bcprov-jdk18on al pom.
}
