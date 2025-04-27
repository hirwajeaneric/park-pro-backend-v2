package com.park.parkpro.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class StripeService {
    private static final Logger LOGGER = Logger.getLogger(StripeService.class.getName());

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        LOGGER.info("Stripe API key initialized");
    }

    public PaymentIntent createPaymentIntent(Long amountInCents, String currency, String description, String metadataBookingId) throws StripeException {
        LOGGER.info("Creating PaymentIntent: amount=" + amountInCents + ", currency=" + currency + ", description=" + description);

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInCents);
        params.put("currency", currency);
        params.put("description", description);
        params.put("metadata", Map.of("booking_id", metadataBookingId));
        params.put("payment_method_types", new String[]{"card"}); // Restrict to card payments

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            LOGGER.info("Created PaymentIntent: ID=" + paymentIntent.getId() + ", Status=" + paymentIntent.getStatus());
            return paymentIntent;
        } catch (StripeException e) {
            LOGGER.severe("Stripe error: " + e.getMessage() + ", Request ID: " + e.getRequestId());
            throw e;
        }
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        LOGGER.info("Confirming PaymentIntent: ID=" + paymentIntentId);
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        if ("requires_confirmation".equals(paymentIntent.getStatus())) {
            paymentIntent = paymentIntent.confirm();
            LOGGER.info("Confirmed PaymentIntent: ID=" + paymentIntent.getId() + ", Status=" + paymentIntent.getStatus());
        } else {
            LOGGER.info("PaymentIntent already in state: " + paymentIntent.getStatus());
        }
        return paymentIntent;
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        LOGGER.info("Retrieving PaymentIntent: ID=" + paymentIntentId);
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        LOGGER.info("Retrieved PaymentIntent: ID=" + paymentIntent.getId() + ", Status=" + paymentIntent.getStatus());
        return paymentIntent;
    }
}