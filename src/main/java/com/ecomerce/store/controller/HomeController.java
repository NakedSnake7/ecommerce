package com.ecomerce.store.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;   
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model; // Importa el Model correcto
import com.ecomerce.store.service.ProductoService;

import jakarta.servlet.http.HttpServletRequest;

//import com.WeedTitlan.server.dto.ProductoResumenDTO;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.repository.ResenaRepository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Controller
public class HomeController {

    @Autowired
    private ProductoService productoService;
    
    private final ResenaRepository resenaRepository;

    public HomeController(ResenaRepository reseñaRepository) {
        this.resenaRepository = reseñaRepository;
    }

    private void cargarDatosMenu(Model model) {
        List<String> categorias = productoService.obtenerCategorias();
        List<Producto> productos = productoService.obtenerProductosVisiblesConTodo();

        model.addAttribute("categorias", categorias);
        model.addAttribute("productos", productos);
    }

    @GetMapping({"/", "/inicio"})
    public String home(Model model, HttpServletRequest request) {

        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String host = forwardedHost != null ? forwardedHost : request.getServerName();

        // 👉 LANDING EDITORIAL
        if (host != null && host.startsWith("espacio.")) {
            return "landing-espacio"; // templates/landing-espacio.html
        }

        // 👉 ECOMMERCE NORMAL
        cargarDatosMenu(model);

        model.addAttribute(
            "resenasIniciales",
            resenaRepository.findAll(
                PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "estrellas"))
            ).getContent()
        );

        return "index";
    }

    @GetMapping("/landing-espacio")
    public String blockLandingDirect() {
        return "redirect:/";
    }


    @GetMapping("/menu")
    public String verMenu(Model model) {
        cargarDatosMenu(model);
        return "index";
    }

    @GetMapping("/subirProducto")
    public String subirProducto(Model model, @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return "redirect:/login";
        }
        return "subirProducto";
    }
    @GetMapping("/fragmento-menu")
    public String cargarFragmentoMenu(Model model) {
        List<String> categorias = productoService.obtenerCategorias();
        List<Producto> productos = productoService.obtenerProductosVisiblesConTodo();

        model.addAttribute("categorias", categorias);
        model.addAttribute("productos", productos);

        // archivo: templates/fragments/menu.html
        // fragmento: menuFragment
        return "fragments/menu :: menu";
    }
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




    @GetMapping("/lista")
    public String listarResenas(Model model) {
    	model.addAttribute("resenas", resenaRepository.findAllByOrderByEstrellasDesc());
        return "resenas";
    }
    @GetMapping("/checkout-cancel")
    public String checkoutCancel() {
        return "checkout-cancel";
    }
    @GetMapping("/gracias")
    public String gracias() {
        return "gracias";
    }
    @GetMapping("/servicio")
    public String servicio() {
        return "servicio"; 
    }
    @GetMapping("/privacy")
    public String privacy() {
        return "privacy"; 
    }



}
