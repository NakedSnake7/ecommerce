package com.ecommerce.store.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.store.model.ProductoVariante;
import com.ecommerce.store.repository.ProductoVarianteRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductoVarianteService {

    @Autowired
    private ProductoVarianteRepository repository;

    @Transactional
    public void actualizarPrecio(Long id, BigDecimal precio) {

        ProductoVariante v = repository.findById(id)
                .orElseThrow();

        v.setPrecio(precio);
    }
}
