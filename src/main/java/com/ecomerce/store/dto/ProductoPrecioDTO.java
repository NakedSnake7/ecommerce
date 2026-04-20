package com.ecomerce.store.dto;

import java.math.BigDecimal;

public class ProductoPrecioDTO {

    private Long id;
    private String productName;
    private BigDecimal price;
    private BigDecimal precioMinimo;
    private boolean tieneVariantes;
    private boolean tienePromocion;
    private String categoriaNombre;


	public ProductoPrecioDTO(Long id, String productName, BigDecimal price, BigDecimal precioMinimo,
			boolean tieneVariantes, boolean tienePromocion, String categoriaNombre) {
		super();
		this.id = id;
		this.productName = productName;
		this.price = price;
		this.precioMinimo = precioMinimo;
		this.tieneVariantes = tieneVariantes;
		this.tienePromocion = tienePromocion;
		this.categoriaNombre = categoriaNombre;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getProductName() {
		return productName;
	}


	public void setProductName(String productName) {
		this.productName = productName;
	}


	public BigDecimal getPrice() {
		return price;
	}


	public void setPrice(BigDecimal price) {
		this.price = price;
	}


	public BigDecimal getPrecioMinimo() {
		return precioMinimo;
	}


	public void setPrecioMinimo(BigDecimal precioMinimo) {
		this.precioMinimo = precioMinimo;
	}


	public boolean isTieneVariantes() {
		return tieneVariantes;
	}


	public void setTieneVariantes(boolean tieneVariantes) {
		this.tieneVariantes = tieneVariantes;
	}


	public boolean isTienePromocion() {
		return tienePromocion;
	}


	public void setTienePromocion(boolean tienePromocion) {
		this.tienePromocion = tienePromocion;
	}


	public String getCategoriaNombre() {
		return categoriaNombre;
	}


	public void setCategoriaNombre(String categoriaNombre) {
		this.categoriaNombre = categoriaNombre;
	}
	
}

