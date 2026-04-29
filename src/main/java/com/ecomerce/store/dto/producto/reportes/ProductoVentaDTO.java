package com.ecomerce.store.dto.producto.reportes;

public class ProductoVentaDTO {

    private String productName;
    private Long totalQuantity;

    public ProductoVentaDTO(String productName, Long totalQuantity) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }
}
