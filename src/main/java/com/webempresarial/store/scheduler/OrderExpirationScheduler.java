package com.webempresarial.store.scheduler;

import org.springframework.scheduling.annotation.Scheduled; 
import org.springframework.stereotype.Component;

import com.webempresarial.store.model.Order;
import com.webempresarial.store.repository.OrderRepository;
import com.webempresarial.store.service.OrderService;

import java.util.List;

@Component
public class OrderExpirationScheduler {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderExpirationScheduler(
            OrderService orderService,
            OrderRepository orderRepository
    ) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    // ⏰ Cada 30 minutos exactos
    @Scheduled(cron = "0 */30 * * * *")
    public void verificarOrdenesPendientes() {

        System.out.println("🔎 OrderExpirationScheduler: buscando órdenes pendientes...");

        List<Order> ordenes = orderRepository.findPendingOrdersWithItems();

        for (Order order : ordenes) {
            // 🔥 TODA la lógica (BD + correo) vive en el service
            orderService.expirarOrdenTransferencia(order);
        }

        System.out.println("✔️ OrderExpirationScheduler finalizado.");
    }
}
