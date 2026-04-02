package com.ecomerce.store.model;

import java.math.BigDecimal; 
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "producto_variantes")
public class ProductoVariante {
	
	@Column(nullable = false)
	private Boolean principal = false;
	
	

	public String getNombreVisual() {

	    if (atributos == null || atributos.isEmpty()) {
	        return "Variante";
	    }

	    return atributos.stream()
	            .map(attr -> attr.getNombre() + ": " + attr.getValor())
	            .collect(Collectors.joining(" | "));
	}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con producto base
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    @Min(0)
    private Integer stock = 0;

    @DecimalMin("0.0")
    private BigDecimal precio;
    

    // 🔥 ATRIBUTOS DINÁMICOS (lo importante del cambio)
    @JsonManagedReference
    @OneToMany(
        mappedBy = "variante",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<VarianteAtributo> atributos = new ArrayList<>();
    
    public void addAtributo(VarianteAtributo atributo) {
        atributo.setVariante(this);
        atributos.add(atributo);
    }
    
    

    // -----------------------------
    // HELPERS
    // -----------------------------

    public BigDecimal getPrecioFinal() {
        return precio != null ? precio : producto.getPrice();
    }

    public void agregarAtributo(String nombre, String valor) {
        VarianteAtributo attr = new VarianteAtributo();
        attr.setNombre(nombre);
        attr.setValor(valor);
        attr.setVariante(this);
        atributos.add(attr);
    }

    // -----------------------------
    // GETTERS / SETTERS
    // -----------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public List<VarianteAtributo> getAtributos() {
        return atributos;
    }
    
    public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

   
    public void setAtributos(List<VarianteAtributo> atributos) {
        this.atributos.clear();

        if (atributos != null) {
            for (VarianteAtributo attr : atributos) {
                attr.setVariante(this);
                this.atributos.add(attr);
            }
        }
    }
}