package com.ecomerce.store.dto.producto.publico;

import java.math.BigDecimal;
import java.util.List;

import com.ecomerce.store.dto.producto.shared.ProductoVarianteDTO;

public class ProductoDetailDTO {

    private Long id;
    private String productName;
    private String description;

    private BigDecimal precio;
    private BigDecimal precioMinimo;
    private BigDecimal precioConDescuento;

    private String imageUrl;

    private Long categoriaId;
    private String categoriaNombre;

    private Long marcaId;
    private String marcaNombre;

    private boolean visibleEnMenu;

    private Double porcentajeDescuento;
    private boolean tienePromocion;

    private Integer stockSimple;

    private List<ProductoVarianteDTO> variantes;

    private List<String> imagenes;



    // GETTERS / SETTERS (simplificados)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getPrecioMinimo() { return precioMinimo; }
    public void setPrecioMinimo(BigDecimal precioMinimo) { this.precioMinimo = precioMinimo; }

    public List<ProductoVarianteDTO> getVariantes() { return variantes; }
    public void setVariantes(List<ProductoVarianteDTO> variantes) { this.variantes = variantes; }
    
    public boolean isTieneVariantes() {
        return variantes != null && !variantes.isEmpty();
    }

    public Integer getStockSimple() { return stockSimple; }
    public void setStockSimple(Integer stockSimple) { this.stockSimple = stockSimple; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public String getMarcaNombre() { return marcaNombre; }
    public void setMarcaNombre(String marcaNombre) { this.marcaNombre = marcaNombre; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public Long getMarcaId() { return marcaId; }
    public void setMarcaId(Long marcaId) { this.marcaId = marcaId; }

    public boolean isVisibleEnMenu() { return visibleEnMenu; }
    public void setVisibleEnMenu(boolean visibleEnMenu) { this.visibleEnMenu = visibleEnMenu; }

    public Double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(Double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public boolean isTienePromocion() { return tienePromocion; }
    public void setTienePromocion(boolean tienePromocion) { this.tienePromocion = tienePromocion; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public BigDecimal getPrecioConDescuento() {
	    return precioConDescuento;
	}

	public void setPrecioConDescuento(BigDecimal precioConDescuento) {
	    this.precioConDescuento = precioConDescuento;
	}
	
}