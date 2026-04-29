package com.ecomerce.store.controller;

import com.ecomerce.store.dto.producto.publico.ProductoDetailDTO;
import com.ecomerce.store.repository.ResenaRepository;
import com.ecomerce.store.service.ProductoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class HomeController {

    private final ProductoService productoService;
    private final ResenaRepository resenaRepository;

    public HomeController(ProductoService productoService, ResenaRepository resenaRepository) {
        this.productoService = productoService;
        this.resenaRepository = resenaRepository;
    }

    // =========================
    // DATA GLOBAL (REUTILIZABLE)
    // =========================
    private void cargarDatosGlobales(Model model) {

        List<String> categorias =
            productoService.obtenerCategorias();

        var productos =
            productoService.obtenerProductosIndexOptimizado();

        model.addAttribute("categorias", categorias);
        model.addAttribute("products", productos);
    }

    // =========================
    //  HOME
    // =========================
    @GetMapping({"/", "/inicio"})
    public String home(Model model, HttpServletRequest request) {

        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String host = forwardedHost != null ? forwardedHost : request.getServerName();

        // 🔥 MULTI-TENANT
        if (host != null && host.startsWith("espacio.")) {
            return "landing-espacio";
        }

        // 🔥 DATA GLOBAL
        cargarDatosGlobales(model);

        // 🔥 RESEÑAS
        model.addAttribute(
            "resenasIniciales",
            resenaRepository.findAll(
                PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "estrellas"))
            ).getContent()
        );
        return "index";
    }

    // =========================
    //  BLOQUEAR LANDING DIRECTO
    // =========================
    @GetMapping("/landing-espacio")
    public String blockLandingDirect() {
        return "redirect:/";
    }

    // =========================
    //  MENÚ
    // =========================
    @GetMapping("/menu")
    public String verMenu(Model model) {
        cargarDatosGlobales(model);
        return "index";
    }

    // =========================
    //  SUBIR PRODUCTO (PROTEGIDO)
    // =========================
    @GetMapping("/subirProducto")
    public String subirProducto(@AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return "redirect:/login";
        }
        return "subirProducto";
    }

    // =========================
    //  FRAGMENTO MENÚ (AJAX)
    // =========================
    @GetMapping("/fragmento-menu")
    public String cargarFragmentoMenu(Model model) {

        cargarDatosGlobales(model);

        return "fragments/menu :: menu";
    }
    
    @GetMapping("/producto-detalle/{id}")
    public String verDetalleProducto(@PathVariable Long id, Model model) {

    	ProductoDetailDTO producto = productoService.obtenerDetalleProducto(id);
        model.addAttribute("producto", producto);

        return "producto-detalle"; // 👈 tu HTML
    }
    // =========================
    //  FRAGMENTO RESEÑAS
    // =========================
    @GetMapping("/fragmento-resenas")
    public String cargarResenasFragment(Model model) {

        model.addAttribute(
            "resenas",
            resenaRepository.findAll(
                Sort.by(Sort.Direction.DESC, "estrellas")
            )
        );

        return "fragments/resenas :: resenas";
    }

    // =========================
    //  LISTA COMPLETA RESEÑAS
    // =========================
    @GetMapping("/lista")
    public String listarResenas(Model model) {

        model.addAttribute(
            "resenas",
            resenaRepository.findAllByOrderByEstrellasDesc()
        );

        return "resenas";
    }

    // =========================
    //  CHECKOUT CANCELADO
    // =========================
    @GetMapping("/checkout-cancel")
    public String checkoutCancel() {
        return "checkout-cancel";
    }

    // =========================
    //  COMPRA EXITOSA
    // =========================
    @GetMapping("/gracias")
    public String gracias() {
        return "gracias";
    }

    // =========================
    //  SERVICIO
    // =========================
    @GetMapping("/servicio")
    public String servicio() {
        return "servicio";
    }

    // =========================
    //  PRIVACIDAD
    // =========================
    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }
}