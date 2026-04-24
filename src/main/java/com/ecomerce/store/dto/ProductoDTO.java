package com.ecomerce.store.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.*;

public class ProductoDTO {

    private Long id;

    @NotBlank
    @Size(min = 3, max = 80)
    private String productName;
    
    private Long marcaId;
    private String marcaNombre;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal precio;

    private String description;

    private Long categoriaId;
    private String categoriaNombre;
    private String nuevaCategoria;

    private String imageUrl;
    private Integer stockSimple;

    
    //  VARIANTES (modelo Amazon)
    private List<ProductoVarianteDTO> variantes = new ArrayList<>();


    private Double porcentajeDescuento = 0.0;
    private boolean visibleEnMenu = true;
    private Boolean tienePromocion = false;

    //  PRECIOS CALCULADOS (clave para frontend limpio)
    private BigDecimal precioFinal;
    private BigDecimal precioMinimo;

    // IMÁGENES EXISTENTES
    private List<Long> imagenesExistentes = new ArrayList<>();
    private List<String> urlsImagenesExistentes = new ArrayList<>();
    private List<Long> imagenesEliminar;
    
    public BigDecimal getPrecioConDescuento() {
        if (tienePromocion != null && tienePromocion && porcentajeDescuento != null && porcentajeDescuento > 0) {

            BigDecimal descuento = precio
                    .multiply(BigDecimal.valueOf(porcentajeDescuento))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            return precio.subtract(descuento);
        }
        return precio;
    }

    // =========================
    // HELPERS (IMPORTANTE)
    // =========================

    public boolean isTieneVariantes() {
        return variantes != null && !variantes.isEmpty();
    }
    
    public boolean getTieneVariantes() {
        return isTieneVariantes();
    }

    public boolean tieneStock() {
        return getStockSimple() > 0;
    }

    public int getTotalVariantes() {
        return variantes != null ? variantes.size() : 0;
    }

    // =========================
    // GETTERS / SETTERS
    // =========================

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

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getPrice() {
        return precio;
    }

    public void setPrice(BigDecimal price) {
        this.precio = price != null
                ? price.setScale(2, RoundingMode.HALF_UP)
                : null;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio != null
                ? precio.setScale(2, RoundingMode.HALF_UP)
                : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getNuevaCategoria() {
        return nuevaCategoria;
    }

    public void setNuevaCategoria(String nuevaCategoria) {
        this.nuevaCategoria = nuevaCategoria;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getStockSimple() {
        return stockSimple != null ? stockSimple : 0;
    }

    public void setStockSimple(Integer stockSimple) {
        this.stockSimple = stockSimple != null ? stockSimple : 0;
    }

    public List<ProductoVarianteDTO> getVariantes() {
        return variantes != null ? variantes : new ArrayList<>();
    }

    public void setVariantes(List<ProductoVarianteDTO> variantes) {
        this.variantes = (variantes != null) ? variantes : new ArrayList<>();
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public Boolean getTienePromocion() {
        return tienePromocion;
    }

    public void setTienePromocion(Boolean tienePromocion) {
        this.tienePromocion = tienePromocion;
    }


  

    public BigDecimal getPrecioFinal() {
        return precioFinal;
    }

    public void setPrecioFinal(BigDecimal precioFinal) {
        this.precioFinal = precioFinal;
    }

    public BigDecimal getPrecioMinimo() {
        return precioMinimo;
    }

    public void setPrecioMinimo(BigDecimal precioMinimo) {
        this.precioMinimo = precioMinimo;
    }

    public List<Long> getImagenesExistentes() {
        return imagenesExistentes;
    }

    public void setImagenesExistentes(List<Long> imagenesExistentes) {
        this.imagenesExistentes = imagenesExistentes;
    }

    public List<String> getUrlsImagenesExistentes() {
        return urlsImagenesExistentes;
    }

    public void setUrlsImagenesExistentes(List<String> urlsImagenesExistentes) {
        this.urlsImagenesExistentes = urlsImagenesExistentes;
    }
    public boolean isVisibleEnMenu() {
        return visibleEnMenu;
    }

    public void setVisibleEnMenu(boolean visibleEnMenu) {
        this.visibleEnMenu = visibleEnMenu;
    }

	public List<Long> getImagenesEliminar() {
		return imagenesEliminar;
	}

	public void setImagenesEliminar(List<Long> imagenesEliminar) {
		this.imagenesEliminar = imagenesEliminar;
	}
	
	public Long getMarcaId() {
	    return marcaId;
	}

	public void setMarcaId(Long marcaId) {
	    this.marcaId = marcaId;
	}

	public String getMarcaNombre() {
	    return marcaNombre;
	}

	public void setMarcaNombre(String marcaNombre) {
	    this.marcaNombre = marcaNombre;
	}
    
}