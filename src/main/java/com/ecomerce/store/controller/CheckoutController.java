package com.ecomerce.store.controller;

import com.ecomerce.store.dto.CheckoutRequestDTO;   
import com.ecomerce.store.model.*;
import com.ecomerce.store.model.Order.PaymentMethod;
import com.ecomerce.store.repository.ProductoRepository;
import com.ecomerce.store.service.OrderService;
import com.ecomerce.store.service.UserService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private static final Logger logger =
            LoggerFactory.getLogger(CheckoutController.class);

    private static final double LIMITE_ENVIO_GRATIS = 1250.0;
    private static final double COSTO_ENVIO = 120.0;

    private final OrderService orderService;
    private final UserService userService;
    private final ProductoRepository productoRepository;

    public CheckoutController(
            OrderService orderService,
            UserService userService,
            ProductoRepository productoRepository
    ) {
        this.orderService = orderService;
        this.userService = userService;
        this.productoRepository = productoRepository;
    }

    @PostMapping("/checkout")
    @CrossOrigin(origins = {
            "http://localhost:8080",
            "https://weedtlanmx.com"
    })
    public ResponseEntity<?> processCheckout(
            @Valid @RequestBody CheckoutRequestDTO checkoutRequest
    ) {

        try {
            // 1️⃣ Validar stock (lock)
            orderService.validarStockCheckout(checkoutRequest);

            // 2️⃣ Validar dirección
            String direccion = checkoutRequest.getCustomer().getAddress();
            if (direccion == null || direccion.trim().length() < 5) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "La dirección debe tener al menos 5 caracteres"
                ));
            }

            // 3️⃣ Usuario
            User user = userService.findOrCreateUserByEmail(
                    checkoutRequest.getCustomer().getEmail(),
                    checkoutRequest.getCustomer().getFullName(),
                    checkoutRequest.getCustomer().getPhone()
            );

            // 🔥 SINCRONIZAR DIRECCIÓN
            String direccionCheckout = checkoutRequest.getCustomer().getAddress();

            if (direccionCheckout != null && direccionCheckout.trim().length() >= 5) {
                // Solo actualiza si es nueva o diferente
                if (user.getDefaultAddress() == null ||
                    !user.getDefaultAddress().equalsIgnoreCase(direccionCheckout.trim())) {

                    user.setDefaultAddress(direccionCheckout.trim());
                }
            }

            userService.saveUser(user);


            // 4️⃣ Totales
            double subtotal = checkoutRequest.getCart().stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity())
                    .sum();

            double envio = subtotal >= LIMITE_ENVIO_GRATIS
                    ? 0.0
                    : COSTO_ENVIO;

            double totalFinal = subtotal + envio;

            // 5️⃣ Método de pago
            PaymentMethod metodoPago =
                    "STRIPE".equalsIgnoreCase(
                            checkoutRequest.getPaymentMethod()
                    )
                            ? PaymentMethod.STRIPE
                            : PaymentMethod.TRANSFER;

            // 6️⃣ Crear orden
            String emailNormalizado = checkoutRequest.getCustomer()
                    .getEmail()
                    .trim()
                    .toLowerCase();

            Order order = new Order(
                user,
                totalFinal,
                direccion,
                checkoutRequest.getCustomer().getFullName(),
                emailNormalizado
            );


            
            order.setPaymentMethod(metodoPago);
            order.setPaymentStatus(PaymentStatus.PENDING);

            // 7️⃣ Items
            checkoutRequest.getCart().forEach(cartItem -> {
                Producto producto = productoRepository
                        .findByProductNameConTodo(cartItem.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Producto no encontrado: "
                                                + cartItem.getName()
                                ));

                OrderItem item = new OrderItem(
                        producto,
                        cartItem.getQuantity(),
                        cartItem.getPrice(),
                        order
                );
                order.addItem(item);
            });

         // 8️⃣ Guardar orden (según método de pago)
            if (metodoPago == PaymentMethod.TRANSFER) {
                orderService.saveOrderTransferencia(order);
            } else {
                orderService.crearOrden(order);
            }

            // 🔥 IMPORTANTE:
            // El correo de transferencia
            // se envía DESDE OrderService
            // usando transferInstructionsSent

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "orderId", order.getId(),
                    "message", "¡Orden creada correctamente!"
            ));

        } catch (Exception e) {
            logger.error("Error en checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error al procesar la orden"
                    ));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        ));

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "errors", errors
        ));
    }
    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow();

        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "phone", user.getPhone(),
                "address", user.getDefaultAddress()
        ));
    }

}

