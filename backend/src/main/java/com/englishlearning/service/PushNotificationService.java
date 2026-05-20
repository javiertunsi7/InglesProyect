package com.englishlearning.service;

import com.englishlearning.config.VapidConfig;
import com.englishlearning.domain.model.PushSubscription;
import com.englishlearning.dto.PushSubscribeRequest;
import com.englishlearning.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final PushSubscriptionRepository repository;
    private final VapidConfig vapidConfig;

    @Transactional
    public void subscribe(Long userId, PushSubscribeRequest request) {
        if (repository.existsByUserIdAndEndpoint(userId, request.getEndpoint())) {
            return;
        }
        PushSubscription sub = PushSubscription.builder()
                .userId(userId)
                .endpoint(request.getEndpoint())
                .p256dhKey(request.getKeys().getP256dh())
                .authKey(request.getKeys().getAuth())
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(sub);
    }

    @Transactional
    public void unsubscribe(String endpoint) {
        repository.deleteByEndpoint(endpoint);
    }

    public void sendToUser(Long userId, String title, String body, String url) {
        List<PushSubscription> subs = repository.findByUserId(userId);
        for (PushSubscription sub : subs) {
            try {
                send(sub, title, body, url);
            } catch (Exception e) {
                log.warn("Failed to send push to subscription {}: {}", sub.getId(), e.getMessage());
            }
        }
    }

    public void sendTest(Long userId) {
        sendToUser(userId,
                "¡Notificación de prueba!",
                "Esta es una notificación de prueba desde enclave.",
                "/");
    }

    private void send(PushSubscription sub, String title, String body, String url) {
        try {
            String payload = String.format(
                    "{\"title\":\"%s\",\"body\":\"%s\",\"url\":\"%s\",\"icon\":\"/pwa-192x192.png\",\"badge\":\"/pwa-64x64.png\"}",
                    escape(title), escape(body), escape(url));

            nl.martijndwars.webpush.PushService pushService = new nl.martijndwars.webpush.PushService();
            pushService.setPublicKey(vapidConfig.getPublicKey());
            pushService.setPrivateKey(vapidConfig.getPrivateKey());

            Subscription.Keys keys = new Subscription.Keys(sub.getAuthKey(), sub.getP256dhKey());
            Subscription subscription = new Subscription(sub.getEndpoint(), keys);

            HttpResponse response = pushService.send(subscription, payload.getBytes(StandardCharsets.UTF_8));

            if (response.getStatusLine().getStatusCode() != 201) {
                log.warn("Push send returned {}: {}", response.getStatusLine().getStatusCode(),
                        response.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            log.error("Error sending push notification: {}", e.getMessage());
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
