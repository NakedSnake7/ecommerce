package com.ecomerce.store.service;

import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional;

import com.ecomerce.store.model.Order;
import com.ecomerce.store.model.PaymentStatus;
import com.stripe.model.checkout.Session;

@Service
public class StripeWebhookService {

    private final OrderService orderService;

    public StripeWebhookService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Transactional
    public void procesarCheckoutCompleted(Session session) {

        // 1️⃣ Validar pago real
        if (!"paid".equals(session.getPaymentStatus())) {
            return;
        }

        String orderIdMeta = session.getMetadata().get("order_id");
        if (orderIdMeta == null) {
            throw new IllegalStateException("Stripe session sin order_id");
        }

        Long orderId = Long.valueOf(orderIdMeta);
        Order order = orderService.getById(orderId);

        // 2️⃣ Idempotencia Stripe
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        // 3️⃣ Validar monto
        Long expected = Math.round(order.getTotal() * 100);
        if (!session.getAmountTotal().equals(expected)) {
            return;
        }

        // 4️⃣ Marcar como pagada (CRÍTICO)
        orderService.marcarOrdenComoPagada(
                orderId,
                session.getPaymentIntent()
        );

        // 5️⃣ Post-pago (stock, etc.)
        orderService.procesarPostPago(orderId);

        // 6️⃣ 🔥 CORREO CENTRALIZADO
        orderService.enviarCorreoConfirmacionPagoSiAplica(orderId);
    }
}
