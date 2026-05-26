package com.englishlearning.controller;

import com.englishlearning.config.VapidConfig;
import com.englishlearning.dto.PushSubscribeRequest;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/push")
@RequiredArgsConstructor
public class PushController {

    private final PushNotificationService pushService;
    private final VapidConfig vapidConfig;

    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidConfig.getPublicKey()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody PushSubscribeRequest request) {
        pushService.subscribe(user.id(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody Map<String, String> body) {
        pushService.unsubscribe(body.get("endpoint"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> sendTest(@AuthenticationPrincipal AuthenticatedUser user) {
        pushService.sendTest(user.id());
        return ResponseEntity.ok().build();
    }
}
