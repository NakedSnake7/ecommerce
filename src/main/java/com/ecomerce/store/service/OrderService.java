package com.ecomerce.store.service;

import com.ecomerce.store.exceptions.OrderNotFoundException;                

import com.ecomerce.store.dto.CheckoutRequestDTO;
import com.ecomerce.store.dto.OrderItemDTO;
import com.ecomerce.store.dto.OrderRequestDTO;
import com.ecomerce.store.dto.ProductSalesDTO;
import com.ecomerce.store.model.Order;
import com.ecomerce.store.model.OrderStatus;
import com.ecomerce.store.model.PaymentStatus;
import com.ecomerce.store.model.User;
import com.ecomerce.store.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockService stockService;
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(
    	    OrderRepository orderRepository,
    	    StockService stockService,
    	    NotificationService notificationService
    	) {
    	    this.orderRepository = orderRepository;
    	    this.stockService = stockService;
    	    this.notificationService = notificationService;
    	}

    public Optional<Order> findByStripeSessionId(String stripeSessionId) {
        return orderRepository.findByStripeSessionId(stripeSessionId);
    }
    

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));
    }

    public List<Order> findOrdersFiltered(
            OrderStatus status,
            PaymentStatus payment,
            LocalDateTime from,
            LocalDateTime to
    ) {
    	return orderRepository.findFilteredWithUser(status, payment, from, to);
    }
    public List<Order> findOrdersForExport(
            OrderStatus status,
            PaymentStatus payment,
            LocalDateTime from,
            LocalDateTime to) {

        return orderRepository.findFilteredWithUser(
            status, payment, from, to
        );
    }

    
    // ============================
    // OBTENER ORDEN
    // ============================
    public Order getOrderByIdWithUser(Long id) {
        return orderRepository.findByIdWithUser(id)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada con ID: " + id));
    }

    public Order getOrderByIdWithUserAndItems(Long id) {
        return orderRepository.findByIdWithUserAndItems(id)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada con ID: " + id));
    }
    public List<Order> getOrdenesPagadas(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findFilteredWithUser(
                null,
                PaymentStatus.PAID,
                from,
                to
        );
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAllWithUser();
    }
    public List<Order> filterOrders(
            LocalDate from,
            LocalDate to,
            OrderStatus status,
            PaymentStatus payment) {

        LocalDateTime fromDate = (from != null)
                ? from.atStartOfDay()
                : null;

        LocalDateTime toDate = (to != null)
                ? to.atTime(23, 59, 59)
                : null;

        return orderRepository.findFilteredWithUser(
                status,
                payment,
                fromDate,
                toDate
        );
    }
    
    /* =====================================================
    reclamar ordenes 
===================================================== */
    @Transactional
    public void claimGuestOrders(User user) {

        String email = user.getEmail().trim().toLowerCase();

        List<Order> orders = orderRepository
                .findByCustomerEmailIgnoreCaseAndUserIsNull(email);

        List<Order> toUpdate = new ArrayList<>();

        for (Order order : orders) {

            if (order.canBeClaimed()) {
                order.claim(user);
                toUpdate.add(order);
            }
        }

        if (!toUpdate.isEmpty()) {
            orderRepository.saveAll(toUpdate);
        }

        System.out.println("Órdenes reclamadas: " + toUpdate.size());
    }
/* =====================================================
    CREACIÓN DE ORDEN
 ===================================================== */
    @Transactional
    public Order crearOrden(Order order) {
        return orderRepository.save(order);
    }
    
    
 /* =====================================================
guardar orden por transferencia
===================================================== */
 
 @Transactional
 public Order saveOrderTransferencia(Order order) {
     Order saved = orderRepository.save(order);
     notificationService.sendTransferInstructions(saved);
     return saved;
 }
 /* =====================================================
    WEBHOOK STRIPE – PASO 1 (CRÍTICO)
    👉 ESTE NUNCA DEBE FALLAR
 ===================================================== */
 @Transactional(propagation = Propagation.REQUIRES_NEW)
 public void marcarOrdenComoPagada(Long orderId, String paymentIntentId) {

     Order order = getById(orderId);

     if (order.getOrderStatus() == OrderStatus.CANCELLED) {
         throw new IllegalStateException("No puedes pagar una orden cancelada");
     }

     order.markAsPaid(paymentIntentId);
     orderRepository.save(order);
 }

 /* =====================================================
    POST-PAGO – PASO 2 (PUEDE FALLAR)
    👉 STOCK / LOGÍSTICA
 ===================================================== */

 @Transactional
 public void procesarPostPago(Long orderId) {

     Order order = getByIdWithUserAndItems(orderId);
     
     if (!order.isPaid()) {
    	    throw new IllegalStateException("No puedes procesar una orden no pagada");
    	}

     // 🔐 Idempotencia
     if (order.isStockReduced()) {
    	    if (order.getOrderStatus() != OrderStatus.PROCESSED) {
    	        order.markAsProcessed();
    	        orderRepository.save(order);
    	    }
    	    return;
    	}

     try {
    	 stockService.descontarStock(order);

    	 
    	 order.markAsProcessed();

         orderRepository.save(order);

     } catch (Exception e) {

         order.markAsPendingStock();
         orderRepository.save(order);

        

         log.error("Stock falló en orden {}", orderId, e);    }
 }
 @Transactional
 public void confirmarPagoTransferencia(Long orderId) {

     Order order = getByIdWithUserAndItems(orderId);

     if (order.isPaid() && order.getOrderStatus() == OrderStatus.PROCESSED) {
         return;
     }

     order.markAsPaid(null);
     orderRepository.save(order);

     try {
         procesarPostPago(orderId);
     } catch (Exception e) {
         log.error("Error post-pago transferencia {}", orderId, e);
     }

     notificationService.sendPaymentConfirmation(order);
 }
    // ============================
    // ACTUALIZAR ESTADO + STOCK
    // ============================
 @Transactional
 public Order updateOrderStatus(Long orderId, String newStatus) {

     Order order = getByIdWithUserAndItems(orderId);

     OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());

     order.changeStatus(status);

     return orderRepository.save(order);
 }
    // ============================
    // ACTUALIZAR INFO DE ENVÍO
    // ============================
    @Transactional
    public Order updateShippingInfo(Long orderId, String tracking, String carrier) {

        Order order = getById(orderId);

        order.markAsShipped(tracking, carrier);

        Order saved = orderRepository.save(order);

        notificationService.sendShipping(saved);

        return saved;
    } 
    // ============================
    // ELIMINAR ORDEN
    // ============================
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Orden no encontrada con ID: " + id);
        }
        orderRepository.deleteById(id);
    }
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
    }
    public Order save(Order order) {
        return orderRepository.save(order);
    }
    @Retryable(
    	    value = { PessimisticLockingFailureException.class, CannotAcquireLockException.class },
    	    maxAttempts = 3,
    	    backoff = @Backoff(delay = 200)
    	)    
    @Transactional
    public void validarStockOrden(OrderRequestDTO request) {
        stockService.validarStock(request.getItems());
    }

    public void validarStockCheckout(CheckoutRequestDTO request) {
        stockService.validarStock(
            request.getCart().stream()
                .map(item -> new OrderItemDTO(
                    item.getVarianteId(),
                    item.getQuantity()
                ))
                .toList()
        );
    }  
/* =====================================================
    EXPIRAR ÓRDENES (TRANSFERENCIAS)
 ===================================================== */
    @Transactional
    public boolean expirarOrdenTransferencia(Order order) {

        if (!order.canExpire()) return false;

        if (order.isStockReduced()) {
            stockService.restaurarStock(order);
        }

        order.markAsExpired();

        orderRepository.save(order);

        notificationService.sendExpired(order, order.getOrderDate().plusHours(24));

        return true;
    }

    public List<ProductSalesDTO> getPaidProductSalesByDate(
            LocalDate from,
            LocalDate to
    ) {
        LocalDateTime fromDT = (from != null)
                ? from.atStartOfDay()
                : null;

        LocalDateTime toDT = (to != null)
                ? to.atTime(23, 59, 59)
                : null;

        return orderRepository.getPaidProductSalesByDate(fromDT, toDT);
    }
    public Order getByIdWithUserAndItems(Long orderId) {
        return orderRepository.findByIdWithUserAndItems(orderId)
                .orElseThrow(() -> new IllegalStateException("Orden no encontrada"));
    }
 // ============================
 // PEDIDOS POR USUARIO (FRONT)
 // ============================
    public List<Order> findByCustomerEmail(String email) {
        return orderRepository
            .findByCustomerEmailOrderByOrderDateDesc(
                email.trim().toLowerCase()
            );
    }
 
 public String obtenerUltimaDireccion(User user) {
	    return orderRepository
	        .findTopByUserOrderByOrderDateDesc(user)
	        .map(Order::getAddress)
	        .orElse(null);
	}

}
