package com.ecomerce.store.dto;

import com.ecomerce.store.contracts.StockItem; 

public class OrderItemDTO implements StockItem {

    private Long varianteId;
    private Integer quantity;

    // 🔥 Constructor vacío (necesario para Jackson)
    public OrderItemDTO() {}

    // 🔥 Constructor con parámetros
    public OrderItemDTO(Long varianteId, Integer quantity) {
        this.varianteId = varianteId;
        this.quantity = quantity;
    }

    @Override
    public Long getVarianteId() {
        return varianteId;
    }

    public void setVarianteId(Long varianteId) {
        this.varianteId = varianteId;
    }

    @Override
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}