package com.park.parkpro.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public PaymentIntent createPaymentIntent(Long amountInCents, String currency, String description, String metadataBookingId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInCents); // Amount in cents (e.g., 5000 for 50.00)
        params.put("currency", currency);    // e.g., "XAF"
        params.put("description", description);
        params.put("metadata", Map.of("booking_id", metadataBookingId)); // Link to booking

        return PaymentIntent.create(params);
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        if ("requires_confirmation".equals(paymentIntent.getStatus())) {
            return paymentIntent.confirm();
        }
        return paymentIntent; // Already confirmed or in another state
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
}