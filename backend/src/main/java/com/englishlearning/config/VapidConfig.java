package com.englishlearning.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

@Component
public class VapidConfig {

    private static final Logger log = LoggerFactory.getLogger(VapidConfig.class);

    @Value("${app.vapid.public-key:}")
    private String configuredPublicKey;

    @Value("${app.vapid.private-key:}")
    private String configuredPrivateKey;

    @Getter
    private String publicKey;

    @Getter
    private String privateKey;

    @PostConstruct
    public void init() {
        if (!configuredPublicKey.isBlank() && !configuredPrivateKey.isBlank()) {
            publicKey = configuredPublicKey.trim();
            privateKey = configuredPrivateKey.trim();
            log.info("VAPID keys loaded from configuration");
        } else {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(new ECGenParameterSpec("secp256r1"));
                KeyPair keyPair = kpg.generateKeyPair();

                ECPublicKey pub = (ECPublicKey) keyPair.getPublic();
                ECPrivateKey priv = (ECPrivateKey) keyPair.getPrivate();

                publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getEncoded());
                privateKey = Base64.getUrlEncoder().withoutPadding().encodeToString(priv.getEncoded());

                log.warn("VAPID keys generated in memory. Set app.vapid.public-key and app.vapid.private-key " +
                        "in application.properties to persist them.\n" +
                        "  Public:  {}\n  Private: {}", publicKey, privateKey);
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
                // El generador EC no acepta la curva o el algoritmo: en ese caso
                // no podemos servir push notifications. Fallar arranque es
                // preferible a un VAPID nulo que rompería /v1/push silenciosamente.
                throw new RuntimeException("Failed to generate VAPID keys", e);
            }
        }
    }
}
