package com.ecomerce.store.dto;

import java.math.BigDecimal;

public class ProductoResumenDTO {

    private Long id;
    private String productName;
    private BigDecimal price;
    private Boolean tienePromocion;
    private Double porcentajeDescuento;
    private String imagenUrl;
    private String categoria;
    private String descripcion;


    public ProductoResumenDTO(Long id, String productName, BigDecimal price,
                              Boolean tienePromocion, Double porcentajeDescuento, String imagenUrl, String categoria,String descripcion) {
        this.id = id;
        this.productName = productName;
        this.price = price;
        this.tienePromocion = tienePromocion != null ? tienePromocion : false;
        this.porcentajeDescuento = porcentajeDescuento != null ? porcentajeDescuento : 0.0;
        this.imagenUrl = imagenUrl;
        this.categoria = categoria; 
        this.setDescripcion(descripcion);
    }

    public BigDecimal getPrecioConDescuento() {
        if (tienePromocion && porcentajeDescuento > 0) {
            BigDecimal descuento = price
                .multiply(BigDecimal.valueOf(porcentajeDescuento))
                .divide(BigDecimal.valueOf(100));

            return price.subtract(descuento);
        }
        return price;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Boolean getTienePromocion() {
        return tienePromocion;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}
