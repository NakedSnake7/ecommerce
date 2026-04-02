package com.ecomerce.store.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecomerce.store.exceptions.InsufficientStockException;
import com.ecomerce.store.exceptions.ResourceNotFoundException;
import com.ecomerce.store.model.Order;
import com.ecomerce.store.model.OrderItem;
import com.ecomerce.store.model.ProductoVariante;
import com.ecomerce.store.repository.ProductoVarianteRepository;
import com.ecomerce.store.contracts.StockItem;

import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final ProductoVarianteRepository varianteRepository;

    public StockService(ProductoVarianteRepository varianteRepository) {
        this.varianteRepository = varianteRepository;
    }

    // ============================
    // VALIDAR STOCK
    // ============================
    @Transactional
    public void validarStock(List<? extends StockItem> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No hay items en la orden");
        }

        for (StockItem item : items) {

            ProductoVariante variante = varianteRepository
                    .findByIdForUpdate(item.getVarianteId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Variante no encontrada")
                    );

            if (variante.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para variante: " + variante.getAtributos()
                );
            }
        }
    }
    
    

    // ============================
    // DESCONTAR STOCK
    // ============================
    @Transactional
    public void descontarStock(Order order) {

        if (order.isStockReduced()) return;

        for (OrderItem item : order.getItems()) {

            ProductoVariante variante = varianteRepository
                    .findByIdForUpdate(item.getVariante().getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Variante no encontrada")
                    );

            if (variante.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para variante: " + variante.getAtributos()
                );
            }

            variante.setStock(variante.getStock() - item.getQuantity());
            varianteRepository.save(variante);
        }

        order.setStockReduced(true);
    }

    // ============================
    // RESTAURAR STOCK
    // ============================
    @Transactional
    public void restaurarStock(Order order) {

        if (!order.isStockReduced()) return;

        for (OrderItem item : order.getItems()) {

            ProductoVariante variante = varianteRepository
                    .findByIdForUpdate(item.getVariante().getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Variante no encontrada")
                    );

            variante.setStock(variante.getStock() + item.getQuantity());
            varianteRepository.save(variante);
        }

        order.setStockReduced(false);
    }
}