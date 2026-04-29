package com.ecomerce.store.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomerce.store.service.ProductoVarianteService;

@RestController
@RequestMapping("/api/variantes")
public class VarianteController {

    @Autowired
    private ProductoVarianteService varianteService;

    @PostMapping("/actualizarPrecio")
    public ResponseEntity<?> actualizarPrecio(
            @RequestParam Long varianteId,
            @RequestParam BigDecimal precio) {

        varianteService.actualizarPrecio(varianteId, precio);

        return ResponseEntity.ok().build();
    }
}