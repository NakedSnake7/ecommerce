package com.ecomerce.store.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ProductoVarianteDTO {

    private Long id;
    
    private String nombre;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;

    private BigDecimal precio;

    // 🔥 atributos dinámicos tipo Amazon
    private Map<String, String> atributos = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = (stock != null) ? stock : 0;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Map<String, String> getAtributos() {
        return atributos;
    }

    public void setAtributos(Map<String, String> atributos) {
        this.atributos = (atributos != null) ? atributos : new HashMap<>();
    }

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}