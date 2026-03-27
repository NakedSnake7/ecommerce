package com.ecomerce.store.controller;

import java.util.Map;     

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecomerce.store.model.Order;
import com.ecomerce.store.model.PaymentStatus;
import com.ecomerce.store.service.OrderService;
import com.ecomerce.store.service.StripeCheckoutService;

import com.stripe.model.checkout.Session;

@RestController
@RequestMapping("/api/stripe")
public class StripeCheckoutController {

    private final OrderService orderService;
    private final StripeCheckoutService stripeCheckoutService;

    public StripeCheckoutController(
            OrderService orderService,
            StripeCheckoutService stripeCheckoutService
    ) {
        this.orderService = orderService;
        this.stripeCheckoutService = stripeCheckoutService;
    }

    @PostMapping("/create-session/{orderId}")
    public ResponseEntity<?> createStripeSession(@PathVariable Long orderId) {

        Order order = orderService.getById(orderId);

        // 🔒 Orden ya pagada
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La orden ya fue pagada"));
        }

        try {

            // ❌ NO reutilizar sesiones abiertas
            if (order.getStripeSessionId() != null) {

                boolean expired =
                    stripeCheckoutService.isSessionExpired(order.getStripeSessionId());

                if (!expired) {
                    return ResponseEntity.ok(
                            Map.of("url",
                                    stripeCheckoutService.getSessionUrl(
                                            order.getStripeSessionId()
                                    )
                            )
                    );
                }

                // ⚠️ Sesión expirada → limpiar
                order.setStripeSessionId(null);
                orderService.save(order);
            }

            // 🆕 Crear sesión nueva
            Session session = stripeCheckoutService.createSession(order);

            order.setStripeSessionId(session.getId());
            orderService.save(order);

            return ResponseEntity.ok(Map.of("url", session.getUrl()));

        } catch (Exception e) {
            System.err.println("❌ Stripe error: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al crear sesión de pago"));
        }
    }

}
