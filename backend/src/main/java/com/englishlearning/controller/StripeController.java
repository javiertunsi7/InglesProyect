package com.englishlearning.controller;

import com.englishlearning.dto.CreateCheckoutRequest;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.StripeService;
import com.englishlearning.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/v1/stripe")
@RequiredArgsConstructor
public class StripeController {

    private static final Logger log = LoggerFactory.getLogger(StripeController.class);

    private final StripeService stripeService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateCheckoutRequest request) throws Exception {
        // AuthenticatedUser es un record → accesors id()/email(), no getId()/getEmail().
        String url = stripeService.createCheckoutSession(
                user.id(), request.getPriceId(), user.email());
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/create-portal-session")
    public ResponseEntity<Map<String, String>> createPortalSession(
            @AuthenticationPrincipal AuthenticatedUser user) throws Exception {
        var sub = subscriptionService.getStatus(user.id());
        if (sub.getPlan() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active subscription"));
        }
        String url = stripeService.createPortalSession(
                subscriptionService.getCustomerId(user.id()));
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            var event = stripeService.verifyWebhook(payload, sigHeader);

            switch (event.getType()) {
                case "checkout.session.completed" -> {
                    var session = (com.stripe.model.checkout.Session) event.getDataObjectDeserializer()
                            .getObject().orElse(null);
                    if (session != null) {
                        handleCheckoutCompleted(session);
                    }
                }
                case "customer.subscription.updated",
                     "customer.subscription.deleted" -> {
                    var stripeSub = (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .getObject().orElse(null);
                    if (stripeSub != null) {
                        handleSubscriptionUpdated(stripeSub);
                    }
                }
                default -> log.debug("Unhandled Stripe event: {}", event.getType());
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Stripe webhook error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook error");
        }
    }

    private void handleCheckoutCompleted(com.stripe.model.checkout.Session session) {
        Long userId = Long.valueOf(session.getClientReferenceId());
        String customerId = session.getCustomer();
        String subId = session.getSubscription();
        // El plan se decide y se persiste en metadata al crear el checkout
        // (ver StripeService). Si por lo que sea falta, abortamos: prefiero un
        // fallo visible a etiquetar mal una suscripción.
        String plan = session.getMetadata() != null ? session.getMetadata().get("plan") : null;
        if (plan == null) {
            log.error("checkout.session.completed sin 'plan' en metadata (user {} sub {})", userId, subId);
            return;
        }

        try {
            var stripeSub = com.stripe.model.Subscription.retrieve(subId);
            subscriptionService.createOrUpdate(
                    userId, customerId, subId, plan,
                    stripeSub.getStatus(),
                    stripeSub.getCurrentPeriodStart() != null
                            ? Instant.ofEpochSecond(stripeSub.getCurrentPeriodStart()) : null,
                    stripeSub.getCurrentPeriodEnd() != null
                            ? Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd()) : null,
                    stripeSub.getTrialEnd() != null
                            ? Instant.ofEpochSecond(stripeSub.getTrialEnd()) : null,
                    stripeSub.getCancelAtPeriodEnd()
            );
            log.info("Subscription created for user {}: {}", userId, subId);
        } catch (Exception e) {
            log.error("Error processing checkout completed for user {}: {}", userId, e.getMessage());
        }
    }

    private void handleSubscriptionUpdated(com.stripe.model.Subscription stripeSub) {
        String customerId = stripeSub.getCustomer();
        try {
            var sub = subscriptionService.findByStripeCustomerId(customerId);
            if (sub != null) {
                subscriptionService.createOrUpdate(
                        sub.getUserId(), customerId, stripeSub.getId(),
                        sub.getPlan(), stripeSub.getStatus(),
                        stripeSub.getCurrentPeriodStart() != null
                                ? Instant.ofEpochSecond(stripeSub.getCurrentPeriodStart()) : null,
                        stripeSub.getCurrentPeriodEnd() != null
                                ? Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd()) : null,
                        stripeSub.getTrialEnd() != null
                                ? Instant.ofEpochSecond(stripeSub.getTrialEnd()) : null,
                        stripeSub.getCancelAtPeriodEnd()
                );
                log.info("Subscription updated for customer {}: {}", customerId, stripeSub.getStatus());
            }
        } catch (Exception e) {
            log.error("Error updating subscription for customer {}: {}", customerId, e.getMessage());
        }
    }
}
