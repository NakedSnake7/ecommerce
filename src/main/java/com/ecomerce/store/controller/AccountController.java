package com.ecomerce.store.controller;

import com.ecomerce.store.model.Order; 
import com.ecomerce.store.model.User;
import com.ecomerce.store.service.OrderService;
import com.ecomerce.store.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AccountController {

    private final UserService userService;
    private final OrderService orderService;

    public AccountController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    // ============================
    // MI CUENTA
    // ============================
    @GetMapping("/cuenta")
    public String cuenta(
            @AuthenticationPrincipal UserDetails authUser,
            Model model) {

        User usuario = userService.findByEmail(authUser.getUsername()).orElseThrow();

        String direccion = usuario.getDefaultAddress();

        if (direccion == null) {
            direccion = orderService.obtenerUltimaDireccion(usuario);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("direccion", direccion);

        return "cuenta";
    }

    
    @PostMapping("/cuenta/actualizar")
    public String actualizarCuenta(
            @AuthenticationPrincipal UserDetails authUser,
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes
    ) {

        if (authUser == null) {
            return "redirect:/login";
        }

        String email = authUser.getUsername();

        User usuario = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar solo campos permitidos
        usuario.setFullName(fullName);
        usuario.setPhone(phone);

        userService.save(usuario);

        redirectAttributes.addFlashAttribute(
                "success",
                "Datos actualizados correctamente"
        );

        return "redirect:/cuenta";
    }
    
    @PostMapping("/cuenta/direccion")
    public String guardarDireccion(
            @AuthenticationPrincipal UserDetails authUser,
            @RequestParam String address) {

        User user = userService.findByEmail(authUser.getUsername()).orElseThrow();

        user.setDefaultAddress(address);
        userService.save(user);

        return "redirect:/cuenta";
    }



    // ============================
    // MIS PEDIDOS
    // ============================
    @GetMapping("/pedidos")
    public String misPedidos(
            @AuthenticationPrincipal UserDetails authUser,
            Model model) {

        if (authUser == null) {
            return "redirect:/login";
        }

        String email = authUser.getUsername();

        List<Order> pedidos = orderService.findByCustomerEmail(email);

        model.addAttribute("pedidos", pedidos);

        return "pedidos";
    }
}
