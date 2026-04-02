package com.ecomerce.store.controller;

import com.ecomerce.store.model.Order;    
import com.ecomerce.store.model.OrderStatus;
import com.ecomerce.store.model.PaymentStatus;
import com.ecomerce.store.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;
    
    
    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
		
    }


    // ================================
    // LISTAR ÓRDENES
    // ================================
    @GetMapping
    public String listarOrders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus payment,
            @RequestParam(required = false) String search,
            Model model) {

        List<Order> orders = orderService.filterOrders(from, to, status, payment);

        // 🔍 Búsqueda por nombre (opcional, en memoria)
        if (search != null && !search.isBlank()) {
            String text = search.toLowerCase();
            orders = orders.stream()
                    .filter(o ->
                            o.getCustomerName() != null &&
                            o.getCustomerName().toLowerCase().contains(text)
                    )
                    .toList();
        }

        // ✅ ORDENAR POR FECHA (más recientes primero)
        orders = orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .toList();

        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());

        return "orders";
    }



    // ================================
    // DETALLES
    // ================================
    @GetMapping("/{id}")
    public String verDetalles(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderByIdWithUserAndItems(id);
        model.addAttribute("order", order);
        return "order-details";
    }


 // ================================
 // CONFIRMAR PAGO (TRANSFERENCIA)
 // ================================
    @PostMapping("/{id}/confirm-payment")
    public String confirmarPagoTransferencia(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.confirmarPagoTransferencia(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Pago confirmado correctamente. Orden aprobada."
            );

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/orders/" + id;
    }



    // ================================
    // ACTUALIZAR ENVÍO
    // ================================
    @PostMapping("/update-shipping")
    public String updateShipping(
            @RequestParam Long orderId,
            @RequestParam String courier,
            @RequestParam String trackingNumber,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.updateShippingInfo(orderId, trackingNumber, courier);
            redirectAttributes.addFlashAttribute("success", "Envío actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar envío");
        }

        return "redirect:/orders/" + orderId;
    }

    // ================================
    // ELIMINAR ORDEN
    // ================================
    @GetMapping("/{id}/delete")
    public String eliminarOrden(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Order order = orderService.getOrderById(id);

        if (order.getPaymentStatus() == PaymentStatus.PAID ||
            order.getOrderStatus() == OrderStatus.CANCELLED) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede eliminar esta orden"
            );
            return "redirect:/orders";
        }

        orderService.deleteOrder(id);
        redirectAttributes.addFlashAttribute("success", "Orden eliminada");

        return "redirect:/orders";
    }
    
    @PostMapping("/{id}/status-ajax")
    @ResponseBody
    public Map<String, String> updateOrderStatusAjax(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        OrderStatus newStatus = OrderStatus.valueOf(payload.get("status"));

        Order order = orderService.getById(id);

        order.setOrderStatus(newStatus);
        orderService.save(order);

        Map<String, String> response = new HashMap<>();
        response.put("label", order.getOrderStatusLabel());
        response.put("badge", order.getOrderStatusBadge());

        return response;
    }


    
}
