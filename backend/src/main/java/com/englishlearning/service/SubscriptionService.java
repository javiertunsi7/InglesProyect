package com.englishlearning.service;

import com.englishlearning.domain.model.UserSubscription;
import com.englishlearning.dto.SubscriptionStatusResponse;
import com.englishlearning.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final UserSubscriptionRepository repository;

    public boolean isPremium(Long userId) {
        return repository.findByUserId(userId)
                .map(this::isActive)
                .orElse(false);
    }

    public SubscriptionStatusResponse getStatus(Long userId) {
        return repository.findByUserId(userId)
                .map(this::toResponse)
                .orElse(SubscriptionStatusResponse.builder()
                        .premium(false)
                        .build());
    }

    @Transactional
    public UserSubscription createOrUpdate(Long userId, String stripeCustomerId,
                                           String stripeSubscriptionId, String plan,
                                           String status, Instant periodStart,
                                           Instant periodEnd, Instant trialEnd,
                                           boolean cancelAtPeriodEnd) {
        Optional<UserSubscription> existing = repository.findByUserId(userId);
        UserSubscription sub = existing.orElse(UserSubscription.builder()
                .userId(userId)
                .createdAt(Instant.now())
                .build());

        sub.setStripeCustomerId(stripeCustomerId);
        sub.setStripeSubscriptionId(stripeSubscriptionId);
        sub.setPlan(plan);
        sub.setStatus(status);
        sub.setCurrentPeriodStart(periodStart);
        sub.setCurrentPeriodEnd(periodEnd);
        sub.setTrialEnd(trialEnd);
        sub.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        sub.setUpdatedAt(Instant.now());

        return repository.save(sub);
    }

    public String getCustomerId(Long userId) {
        return repository.findByUserId(userId)
                .map(UserSubscription::getStripeCustomerId)
                .orElseThrow(() -> new RuntimeException("No subscription found"));
    }

    public UserSubscription findByStripeCustomerId(String customerId) {
        return repository.findByStripeCustomerId(customerId).orElse(null);
    }

    private boolean isActive(UserSubscription sub) {
        return "active".equals(sub.getStatus()) || "trialing".equals(sub.getStatus());
    }

    private SubscriptionStatusResponse toResponse(UserSubscription sub) {
        return SubscriptionStatusResponse.builder()
                .premium(isActive(sub))
                .plan(sub.getPlan())
                .status(sub.getStatus())
                .currentPeriodEnd(sub.getCurrentPeriodEnd() != null
                        ? sub.getCurrentPeriodEnd().toEpochMilli() : null)
                .trialEnd(sub.getTrialEnd() != null
                        ? sub.getTrialEnd().toEpochMilli() : null)
                .cancelAtPeriodEnd(sub.isCancelAtPeriodEnd())
                .build();
    }
}
