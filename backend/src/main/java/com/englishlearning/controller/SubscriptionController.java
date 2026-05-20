package com.englishlearning.controller;

import com.englishlearning.dto.SubscriptionStatusResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/me")
    public ResponseEntity<SubscriptionStatusResponse> getStatus(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(subscriptionService.getStatus(user.getId()));
    }
}
