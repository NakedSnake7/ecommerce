package com.ecomerce.store.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecomerce.store.model.Order;
import com.ecomerce.store.service.OrderService;

@Controller
@RequestMapping("/pedidos")
public class PedidoClienteController {

    private final OrderService orderService;

    public PedidoClienteController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public String verPedidoCliente(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {

        Order order = orderService.getOrderByIdWithUserAndItems(id);

        // 🔐 Validación de seguridad
        if (!order.getCustomerEmail().equals(userDetails.getUsername())) {
            return "error/403";
        }

        model.addAttribute("pedido", order);
        return "pedido-detalle";
    }
}
