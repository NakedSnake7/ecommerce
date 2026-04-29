package com.ecomerce.store.dto.producto.publico;

import java.math.BigDecimal; 
import java.math.RoundingMode;

public class ProductoCardDTO {

    private Long id;
    private String productName;

    private BigDecimal precio;
    private BigDecimal precioMinimo;

    private boolean tienePromocion;
    private Double porcentajeDescuento;

    private String imageUrl;

    private Long categoriaId;
    private String categoriaNombre;
    private String marcaNombre;

    private Integer stockSimple;

    private boolean tieneVariantes;
    private boolean visibleEnMenu = true;

    // =====================================
    // CONSTRUCTOR VACÍO
    // =====================================
    public ProductoCardDTO() {
    }



    // =====================================
    // CONSTRUCTOR OPTIMIZADO HOME
    // =====================================
    public ProductoCardDTO(
            Long id,
            String productName,
            BigDecimal precio,
            BigDecimal precioMinimo,
            Boolean tieneVariantes,
            Boolean tienePromocion,
            Double porcentajeDescuento,
            String imageUrl,
            String categoriaNombre,
            String marcaNombre,
            Integer stockSimple
    ) {
        this.id = id;
        this.productName = productName;
        this.precio = precio;
        this.precioMinimo = precioMinimo != null ? precioMinimo : precio;
        this.tieneVariantes = Boolean.TRUE.equals(tieneVariantes);
        this.tienePromocion = Boolean.TRUE.equals(tienePromocion);
        this.porcentajeDescuento = porcentajeDescuento != null ? porcentajeDescuento : 0.0;
        this.imageUrl = imageUrl != null ? imageUrl : "/img/default.png";
        this.categoriaNombre = categoriaNombre;
        this.marcaNombre = marcaNombre;
        this.stockSimple = stockSimple != null ? stockSimple : 0;
    }

    // =====================================
    // PRECIO FINAL MOSTRAR
    // Si tiene variantes usa precioMinimo
    // =====================================
    public BigDecimal getPrecioConDescuento() {

        BigDecimal base = getPrecioMostrar();

        if (tienePromocion
                && porcentajeDescuento != null
                && porcentajeDescuento > 0
                && base != null) {

            BigDecimal descuento = base
                    .multiply(BigDecimal.valueOf(porcentajeDescuento))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            return base.subtract(descuento);
        }

        return base;
    }

    // =====================================
    // PRECIO MOSTRAR
    // =====================================
    public BigDecimal getPrecioMostrar() {
        if (tieneVariantes && precioMinimo != null) {
            return precioMinimo;
        }
        return precio;
    }

    // =====================================
    // STOCK
    // =====================================
    public boolean isSinStock() {
        return getStockSimple() <= 0;
    }

    // =====================================
    // GETTERS / SETTERS
    // =====================================

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

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getPrecioMinimo() {
        return precioMinimo != null ? precioMinimo : precio;
    }

    public void setPrecioMinimo(BigDecimal precioMinimo) {
        this.precioMinimo = precioMinimo;
    }

    public boolean isTienePromocion() {
        return tienePromocion;
    }

    public void setTienePromocion(boolean tienePromocion) {
        this.tienePromocion = tienePromocion;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "/img/default.png";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getMarcaNombre() {
        return marcaNombre;
    }

    public void setMarcaNombre(String marcaNombre) {
        this.marcaNombre = marcaNombre;
    }

    public Integer getStockSimple() {
        return stockSimple != null ? stockSimple : 0;
    }

    public void setStockSimple(Integer stockSimple) {
        this.stockSimple = stockSimple;
    }

    public boolean isTieneVariantes() {
        return tieneVariantes;
    }

    public void setTieneVariantes(boolean tieneVariantes) {
        this.tieneVariantes = tieneVariantes;
    }

    public boolean isVisibleEnMenu() {
        return visibleEnMenu;
    }

    public void setVisibleEnMenu(boolean visibleEnMenu) {
        this.visibleEnMenu = visibleEnMenu;
    }
}