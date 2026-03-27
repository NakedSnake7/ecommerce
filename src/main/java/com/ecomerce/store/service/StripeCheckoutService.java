package com.ecomerce.store.service;

import com.ecomerce.store.model.Order; 
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StripeCheckoutService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.environment:prod}")
    private String environment;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Session createSession(Order order) throws StripeException {

        // 🔐 Monto seguro (sin errores de redondeo)
        long amountInCents = BigDecimal.valueOf(order.getTotal())
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)

                // ✅ URLs
                .setSuccessUrl(
                	    "https://weedtlanmx.com/gracias.html?session_id={CHECKOUT_SESSION_ID}&order_id=" + order.getId()
                	)
                	.setCancelUrl(
                	    "https://weedtlanmx.com/checkout-cancel.html?order_id=" + order.getId()
                	)


                // ✅ Email (safe)
                .setCustomerEmail(
                        order.getCustomerEmail() != null
                                ? order.getCustomerEmail()
                                : null
                )

                // 🔎 Metadata clave
                .putMetadata("order_id", order.getId().toString())
                .putMetadata("payment_method", "STRIPE")
                .putMetadata("system", "WeedTlanMx")
                .putMetadata("env", environment)

                // 🧾 Referencia visible en Stripe
                .setClientReferenceId("ORDER-" + order.getId())

                // 🛒 Item
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("mxn")
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Orden #" + order.getId() + " – WeedTlanMx")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();

        // 🔐 Idempotencia total
        RequestOptions options = RequestOptions.builder()
            .setIdempotencyKey("order_" + order.getId())
            .build();

        return Session.create(params, options);
    }

    public String getSessionUrl(String sessionId) throws StripeException {

        Session session = Session.retrieve(sessionId);

        if (session == null || session.getUrl() == null) {
            throw new IllegalStateException("Sesión Stripe no válida o expirada");
        }

        return session.getUrl();
    }
    
    
    public boolean isSessionExpired(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return "expired".equals(session.getStatus());
        } catch (Exception e) {
            return true;
        }
    }


}
