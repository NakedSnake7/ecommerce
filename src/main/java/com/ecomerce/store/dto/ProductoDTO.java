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

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal precio;

    private String description;

    private Long categoriaId;
    private String categoriaNombre;
    private String nuevaCategoria;
    
    private String imageUrl;
    private Integer stockTotal;

    // 🔥 VARIANTES (modelo Amazon)
    private List<ProductoVarianteDTO> variantes = new ArrayList<>();

    private Double porcentajeDescuento = 0.0;

    private Boolean visibleEnMenu = true;
    private Boolean tienePromocion = false;

    private List<Long> imagenesExistentes = new ArrayList<>();
    private List<String> urlsImagenesExistentes = new ArrayList<>();
    
    public boolean getTieneVariantes() {
        return variantes != null && !variantes.isEmpty();
    }
    public int getTotalVariantes() {
        return variantes != null ? variantes.size() : 0;
    }
    // -----------------------------
    // GETTERS / SETTERS
    // -----------------------------

    public boolean isVisibleEnMenu() {
        return visibleEnMenu;
    }

    public void setVisibleEnMenu(boolean visibleEnMenu) {
        this.visibleEnMenu = visibleEnMenu;
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
        return precio;
    }

    public void setPrice(BigDecimal price) {
        this.precio = price != null
                ? price.setScale(2, RoundingMode.HALF_UP)
                : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public String getNuevaCategoria() {
        return nuevaCategoria;
    }

    public void setNuevaCategoria(String nuevaCategoria) {
        this.nuevaCategoria = nuevaCategoria;
    }

    public List<ProductoVarianteDTO> getVariantes() {
        return variantes;
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

	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public Integer getStockTotal() {
		return stockTotal;
	}
	public void setStockTotal(Integer stockTotal) {
		this.stockTotal = stockTotal;
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
}