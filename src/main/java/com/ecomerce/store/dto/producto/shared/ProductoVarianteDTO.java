package com.ecomerce.store.dto.producto.shared;


import java.math.BigDecimal; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ProductoVarianteDTO {

    private Long id;
    
    private String nombre;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;

    private BigDecimal precio;

    private String color;

    private String talla;
    //  atributos dinámicos tipo Amazon
    @NotEmpty(message = "Debe tener al menos un atributo")
    private Map<String, String> atributos = new HashMap<>();
    
    private List<String> atributosKeys;
    private List<String> atributosValues;
    
    
    

    public List<String> getAtributosKeys() {
		return atributosKeys;
	}

	public void setAtributosKeys(List<String> atributosKeys) {
		this.atributosKeys = atributosKeys;
	}

	public List<String> getAtributosValues() {
		return atributosValues;
	}

	public void setAtributosValues(List<String> atributosValues) {
		this.atributosValues = atributosValues;
	}

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

    public Map<String,String> getAtributos() {

        if (atributos != null && !atributos.isEmpty()) {
            return atributos;
        }

        Map<String,String> map = new HashMap<>();

        if (atributosKeys != null && atributosValues != null) {

            for (int i = 0; i < atributosKeys.size(); i++) {

                String key = atributosKeys.get(i);
                String value =
                    atributosValues.size() > i
                    ? atributosValues.get(i)
                    : null;

                if (key != null && value != null &&
                    !key.isBlank() && !value.isBlank()) {

                    map.put(key, value);
                }
            }
        }

        return map;
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

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getTalla() {
		return talla;
	}

	public void setTalla(String talla) {
		this.talla = talla;
	}
}