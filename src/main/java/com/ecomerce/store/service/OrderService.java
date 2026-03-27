package com.ecomerce.store.service;

import com.ecomerce.store.exceptions.OrderNotFoundException;            

import com.ecomerce.store.exceptions.ResourceNotFoundException;
import com.ecomerce.store.dto.CheckoutRequestDTO;
import com.ecomerce.store.dto.OrderItemDTO;
import com.ecomerce.store.dto.OrderRequestDTO;
import com.ecomerce.store.dto.ProductSalesDTO;
import com.ecomerce.store.exceptions.InsufficientStockException;
import com.ecomerce.store.model.Order;
import com.ecomerce.store.model.OrderItem;
import com.ecomerce.store.model.OrderStatus;
import com.ecomerce.store.model.PaymentStatus;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.model.User;
import com.ecomerce.store.repository.OrderRepository;
import com.ecomerce.store.repository.ProductoRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service

public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductoRepository productoRepository;
    private final EmailService emailService;

    public OrderService(
            OrderRepository orderRepository,
            ProductoRepository productoRepository,
            EmailService emailService
    ) {
        this.orderRepository = orderRepository;
        this.productoRepository = productoRepository;
        this.emailService = emailService;
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
    CREACIÓN DE ORDEN
 ===================================================== */

 @Transactional
 public Order crearOrden(Order order) {

     order.setPaymentStatus(PaymentStatus.PENDING);
     order.setOrderStatus(OrderStatus.CREATED);
     order.setOrderDate(LocalDateTime.now());
     order.setStockReduced(false);

     return orderRepository.save(order);
 }
 /* =====================================================
guardar orden por transferencia
===================================================== */
 
 @Transactional
 public Order saveOrderTransferencia(Order order) {
     Order saved = orderRepository.save(order);
     enviarCorreoDatosTransferenciaSiAplica(saved.getId());
     return saved;
 }
 /* =====================================================
    WEBHOOK STRIPE – PASO 1 (CRÍTICO)
    👉 ESTE NUNCA DEBE FALLAR
 ===================================================== */

 @Transactional
 public void marcarOrdenComoPagada(Long orderId, String paymentIntentId) {
	 

     Order order = orderRepository.findById(orderId)
             .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));

     // 🔐 Idempotencia
     if (order.getPaymentStatus() == PaymentStatus.PAID) {
         return;
     }

     order.setPaymentStatus(PaymentStatus.PAID);
     order.setPaidAt(LocalDateTime.now());
     order.setPaymentIntentId(paymentIntentId);
     order.setOrderStatus(OrderStatus.PAID_PENDING_STOCK);

     orderRepository.save(order);
     

 }

 /* =====================================================
    POST-PAGO – PASO 2 (PUEDE FALLAR)
    👉 STOCK / LOGÍSTICA
 ===================================================== */

 @Transactional
 public void procesarPostPago(Long orderId) {

     Order order = orderRepository.findByIdWithUserAndItems(orderId)
             .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));

     // 🔐 Idempotencia
     if (order.isStockReduced()) {
    	    if (order.getOrderStatus() != OrderStatus.PROCESSED) {
    	        order.setOrderStatus(OrderStatus.PROCESSED);
    	        orderRepository.save(order);
    	    }
    	    return;
    	}



     try {
         descontarStock(order);
         order.setOrderStatus(OrderStatus.PROCESSED);
         orderRepository.save(order);

     } catch (Exception e) {

         // ⚠️ El pago YA EXISTE → NO rollback
         order.setOrderStatus(OrderStatus.PAID_PENDING_STOCK);
         orderRepository.save(order);

         // Log + alerta
         System.err.println(
             "⚠️ Pago confirmado pero stock falló para orden " + orderId
         );
     }
 }
 @Transactional
 public void confirmarPagoTransferencia(Long orderId) {

     Order order = getByIdWithUserAndItems(orderId);

     if (order.getOrderStatus() == OrderStatus.CANCELLED) {
         throw new IllegalStateException("La orden está expirada");
     }

     // 🔐 Idempotencia total
     if (order.getPaymentStatus() == PaymentStatus.PAID
             && order.getOrderStatus() == OrderStatus.PROCESSED
             && order.isPaymentConfirmedSent()) {
         return;
     }

     // 1️⃣ Estados finales
     order.setPaymentStatus(PaymentStatus.PAID);
     order.setOrderStatus(OrderStatus.PROCESSED);
     order.setPaidAt(LocalDateTime.now());

     orderRepository.save(order);

     // 2️⃣ Correo centralizado
     enviarCorreoConfirmacionPagoSiAplica(orderId);
 }

 @Transactional
 public void enviarCorreoDatosTransferenciaSiAplica(Long orderId) {

     Order order = getByIdWithUserAndItems(orderId);

     // Solo transferencias
     if (order.getPaymentMethod() != Order.PaymentMethod.TRANSFER) {
         return;
     }

     // 🔐 Idempotencia
     if (order.isTransferInstructionsSent()) {
         return;
     }

     try {
         emailService.enviarCorreoDatosTransferencia(order);

         order.setTransferInstructionsSent(true);
         orderRepository.save(order);

     } catch (IOException e) {
         // ⚠️ NO romper creación de orden
         e.printStackTrace();
         System.err.println(
             "⚠️ Error enviando correo datos transferencia. Orden=" + orderId
         );
     }
 }

 @Transactional
 public void enviarCorreoConfirmacionPagoSiAplica(Long orderId) {

     Order order = getByIdWithUserAndItems(orderId);

     // 🔐 Condición única y final
     if (order.getPaymentStatus() == PaymentStatus.PAID
             && order.getOrderStatus() == OrderStatus.PROCESSED
             && !order.isPaymentConfirmedSent()) {

         try {
             emailService.enviarCorreoPedidoProcesado(
                     order.getCustomerEmail(),
                     order.getCustomerName(),
                     order.getId(),
                     order.getItems()
             );

             order.setPaymentConfirmedSent(true);
             orderRepository.save(order);

         } catch (IOException e) {
             // ⚠️ JAMÁS romper flujo de pago
             e.printStackTrace();
             System.err.println(
                     "⚠️ Error enviando correo confirmación pago. Orden=" + orderId
             );
         }
     }
 }

    // ============================
    // ACTUALIZAR ESTADO + STOCK
    // ============================
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {

        Order order = getOrderByIdWithUserAndItems(orderId);

        OrderStatus status;
        try {
            status = OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado no válido: " + newStatus);
        }

        order.setOrderStatus(status);
        return orderRepository.save(order);
    }

    /* =====================================================
    DESCONTAR STOCK (AISLADO)
 ===================================================== */
    @Transactional
    public void descontarStock(Order order) {

        if (order.isStockReduced()) return;

        for (OrderItem item : order.getItems()) {

            Producto producto = productoRepository
                    .findByIdForUpdate(item.getProducto().getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Producto no encontrado")
                    );

            if (producto.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para " + producto.getProductName()
                );
            }

            producto.setStock(producto.getStock() - item.getQuantity());
            productoRepository.save(producto);
        }

        order.setStockReduced(true);
        orderRepository.save(order);
    }

    // ============================
    // ACTUALIZAR INFO DE ENVÍO
    // ============================
    @Transactional
    public Order updateShippingInfo(Long orderId, String trackingNumber, String carrier) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));

        order.setTrackingNumber(trackingNumber);
        order.setCarrier(carrier);

        // 🚚 Estado correcto
        order.setOrderStatus(OrderStatus.SHIPPED);

        Order saved = orderRepository.save(order);

        // 📧 Correo centralizado
        enviarCorreoEnvioSiAplica(orderId);

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
    	    if (request.getItems() == null || request.getItems().isEmpty()) {
    	        throw new IllegalArgumentException("No hay items en la orden");
    	    }

    	    for (OrderItemDTO item : request.getItems()) {
    	        Producto producto = productoRepository.findByIdForUpdate(item.getProductId())
    	                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductId()));

    	        if (producto.getStock() < item.getQuantity()) {
    	            throw new InsufficientStockException(
    	                    "Stock insuficiente para " + producto.getProductName() +
    	                            ". Disponibles: " + producto.getStock());
    	          }
    	        }
    	}
    
/* =====================================================
    EXPIRAR ÓRDENES (TRANSFERENCIAS)
 ===================================================== */

    @Transactional
    public boolean expirarOrdenTransferencia(Order order) {

        if (order.getPaymentMethod() != Order.PaymentMethod.TRANSFER) return false;
        if (order.getPaymentStatus() == PaymentStatus.PAID) return false;
        if (order.getOrderStatus() != OrderStatus.CREATED) return false;

        LocalDateTime limite = order.getOrderDate().plusHours(24);
        if (LocalDateTime.now().isBefore(limite)) return false;

        // 🔁 Revertir stock si estaba apartado
        if (order.isStockReduced()) {
            for (OrderItem item : order.getItems()) {
                Producto producto = productoRepository
                        .findByIdForUpdate(item.getProducto().getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Producto no encontrado")
                        );

                producto.setStock(producto.getStock() + item.getQuantity());
                productoRepository.save(producto);
            }
            order.setStockReduced(false);
        }

        // Estados finales
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.EXPIRED);
        orderRepository.save(order);

        // 🔥 CORREO CENTRALIZADO
        enviarCorreoOrdenExpiradaSiAplica(order.getId(), limite);

        return true;
    }

    @Transactional
    public void validarStockCheckout(CheckoutRequestDTO request) {

        if (request.getCart() == null || request.getCart().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        request.getCart().forEach(item -> {
            Producto producto = productoRepository
                    .findByIdForUpdate(item.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Producto no encontrado: " + item.getProductId()
                            ));

            if (producto.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para " + producto.getProductName() +
                        ". Disponibles: " + producto.getStock()
                );
            }
        });
    }
    @Transactional
    public void enviarCorreoEnvioSiAplica(Long orderId) {

        Order order = getByIdWithUserAndItems(orderId);

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            return;
        }

        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            return;
        }

        // 🔐 Idempotencia
        if (order.isShippingConfirmationSent()) {
            return;
        }

        // Validar datos mínimos
        if (order.getTrackingNumber() == null || order.getCarrier() == null) {
            return;
        }

        try {
            emailService.enviarCorreoEnvio(
                    order.getCustomerEmail(),
                    order.getCustomerName(),
                    order.getId(),
                    order.getOrderDate().toString(), // o fecha de envío si la tienes
                    order.getTrackingNumber(),
                    order.getCarrier()
            );

            order.setShippingConfirmationSent(true);
            orderRepository.save(order);

        } catch (IOException e) {
            // ⚠️ No romper el flujo de logística
            e.printStackTrace();
            System.err.println(
                "⚠️ Error enviando correo de envío. Orden=" + orderId
            );
        }
    }


    @Transactional
    public void enviarCorreoOrdenExpiradaSiAplica(Long orderId, LocalDateTime fechaLimite) {

        Order order = getByIdWithUserAndItems(orderId);

        // Solo transferencias
        if (order.getPaymentMethod() != Order.PaymentMethod.TRANSFER) {
            return;
        }

        // Estados finales esperados
        if (order.getPaymentStatus() != PaymentStatus.EXPIRED
                || order.getOrderStatus() != OrderStatus.CANCELLED) {
            return;
        }

        // 🔐 Idempotencia
        if (order.isOrderExpiredSent()) {
            return;
        }

        try {
            emailService.enviarCorreoOrdenExpirada(order, fechaLimite);

            order.setOrderExpiredSent(true);
            orderRepository.save(order);

        } catch (IOException e) {
            // ⚠️ NO romper el job de expiración
            e.printStackTrace();
            System.err.println(
                "⚠️ Error enviando correo orden expirada. Orden=" + orderId
            );
        }
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
             .findByCustomerEmailOrderByOrderDateDesc(email);
 }
 
 public String obtenerUltimaDireccion(User user) {
	    return orderRepository
	        .findTopByUserOrderByOrderDateDesc(user)
	        .map(Order::getAddress)
	        .orElse(null);
	}



}
