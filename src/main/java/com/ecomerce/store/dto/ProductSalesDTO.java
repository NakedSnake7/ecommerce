package com.ecomerce.store.dto;

public class ProductSalesDTO {

    private String productName;
    private Long totalQuantity;

    public ProductSalesDTO(String productName, Long totalQuantity) {
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
