package com.englishlearning.service;

import com.englishlearning.exception.BadRequestException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.price-monthly}")
    private String monthlyPriceId;

    @Value("${stripe.price-yearly}")
    private String yearlyPriceId;

    @Value("${app.base-url}")
    private String baseUrl;

    public String createCheckoutSession(Long userId, String priceId, String email) throws StripeException {
        // Whitelist: el cliente NO decide qué pagar. Solo aceptamos los dos
        // precios oficiales configurados; cualquier otro priceId activo de la
        // cuenta Stripe (incluido uno con importe 0) sería rechazado aquí.
        if (priceId == null
                || (!priceId.equals(monthlyPriceId) && !priceId.equals(yearlyPriceId))) {
            throw new BadRequestException("priceId no autorizado.");
        }
        String planName = priceId.equals(yearlyPriceId) ? "yearly" : "monthly";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomerEmail(email)
                .setClientReferenceId(userId.toString())
                .setSuccessUrl(baseUrl + "/configuracion?subscription=success")
                .setCancelUrl(baseUrl + "/precios")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build())
                .addPaymentMethodType(com.stripe.param.checkout.SessionCreateParams
                        .PaymentMethodType.CARD)
                .putMetadata("user_id", userId.toString())
                .putMetadata("plan", priceId.equals(yearlyPriceId) ? "yearly" : "monthly")
                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .setTrialPeriodDays(30L)
                                .build())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public String createPortalSession(String stripeCustomerId) throws StripeException {
        com.stripe.param.billingportal.SessionCreateParams params =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                        .setCustomer(stripeCustomerId)
                        .setReturnUrl(baseUrl + "/configuracion")
                        .build();

        com.stripe.model.billingportal.Session portalSession =
                com.stripe.model.billingportal.Session.create(params);
        return portalSession.getUrl();
    }

    public Event verifyWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }
}
