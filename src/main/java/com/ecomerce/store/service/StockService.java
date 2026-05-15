package com.ecomerce.store.service;

import java.util.List;  

import org.springframework.stereotype.Service;

import com.ecomerce.store.exceptions.InsufficientStockException;
import com.ecomerce.store.exceptions.ResourceNotFoundException;
import com.ecomerce.store.model.Order;
import com.ecomerce.store.model.OrderItem;
import com.ecomerce.store.model.ProductoVariante;
import com.ecomerce.store.repository.ProductoRepository;
import com.ecomerce.store.repository.ProductoVarianteRepository;
import com.ecomerce.store.contracts.StockItem;
import com.ecomerce.store.dto.checkout.CartItemDTO;

import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

	private final ProductoVarianteRepository varianteRepository;
	private final ProductoRepository productoRepository;
	
	public StockService(
	        ProductoVarianteRepository varianteRepository,
	        ProductoRepository productoRepository
	) {
	    this.varianteRepository = varianteRepository;
	    this.productoRepository = productoRepository;
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

	        // =========================
	        // VARIANTE
	        // =========================
	        if (item.getVarianteId() != null) {

	            ProductoVariante variante = varianteRepository
	                    .findByIdForUpdate(item.getVarianteId())
	                    .orElseThrow(() ->
	                            new ResourceNotFoundException(
	                                    "Variante no encontrada: "
	                                            + item.getVarianteId()
	                            )
	                    );

	            if (variante.getStock() < item.getQuantity()) {
	                throw new InsufficientStockException(
	                        "Stock insuficiente para variante"
	                );
	            }
	        }

	        // =========================
	        // PRODUCTO SIMPLE
	        // =========================
	        else {

	            CartItemDTO cartItem = (CartItemDTO) item;

	            var producto = productoRepository
	                    .findById(cartItem.getProductId())
	                    .orElseThrow(() ->
	                            new ResourceNotFoundException(
	                                    "Producto no encontrado: "
	                                            + cartItem.getProductId()
	                            )
	                    );

	            if (producto.getStockSimple() < item.getQuantity()) {
	                throw new InsufficientStockException(
	                        "Stock insuficiente para producto: "
	                                + producto.getProductName()
	                );
	            }
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

            // =========================
            // VARIANTE
            // =========================
            if (item.getVariante() != null) {

                ProductoVariante variante = varianteRepository
                        .findByIdForUpdate(item.getVariante().getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Variante no encontrada")
                        );

                if (variante.getStock() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para variante"
                    );
                }

                variante.setStock(
                        variante.getStock() - item.getQuantity()
                );

                varianteRepository.save(variante);

            }

            // =========================
            // PRODUCTO SIMPLE
            // =========================
            else {

                var producto = productoRepository
                        .findById(item.getProducto().getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Producto no encontrado")
                        );

                if (producto.getStockSimple() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para producto: "
                                    + producto.getProductName()
                    );
                }

                producto.setStockSimple(
                        producto.getStockSimple() - item.getQuantity()
                );

                productoRepository.save(producto);
            }
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

            // =========================
            // VARIANTE
            // =========================
            if (item.getVariante() != null) {

                ProductoVariante variante = varianteRepository
                        .findByIdForUpdate(item.getVariante().getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Variante no encontrada")
                        );

                variante.setStock(
                        variante.getStock() + item.getQuantity()
                );

                varianteRepository.save(variante);

            }

            // =========================
            // PRODUCTO SIMPLE
            // =========================
            else {

                var producto = productoRepository
                        .findById(item.getProducto().getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Producto no encontrado")
                        );

                producto.setStockSimple(
                        producto.getStockSimple() + item.getQuantity()
                );

                productoRepository.save(producto);
            }
        }

        order.setStockReduced(false);
    }
}