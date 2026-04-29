package com.ecomerce.store.dto.producto.admin;

import java.math.BigDecimal;
import java.util.List;

import com.ecomerce.store.dto.producto.shared.ProductoVarianteDTO;

public class ProductoAdminListDTO {

    private Long id;
    private String productName;

    private BigDecimal precio;
    private BigDecimal precioMinimo;

    private Double porcentajeDescuento;
    private boolean tienePromocion;

    private String imageUrl;

    private Long categoriaId;
    private String categoriaNombre;

    private String marcaNombre;

    private Integer stockSimple;

    private boolean tieneVariantes;
    private boolean visibleEnMenu;

    // =====================================
    // VARIANTES
    // =====================================
    private List<ProductoVarianteDTO> variantes;

    // =====================================
    // IMÁGENES (opcional si quieres expandir UI luego)
    // =====================================
    private List<String> imagenes;

    // =====================================
    // CONSTRUCTORES
    // =====================================
    public ProductoAdminListDTO() {
    }

    public ProductoAdminListDTO(
            Long id,
            String productName,
            BigDecimal precio,
            BigDecimal precioMinimo,
            Double porcentajeDescuento,
            boolean tienePromocion,
            String imageUrl,
            String categoriaNombre,
            String marcaNombre,
            Integer stockSimple,
            boolean tieneVariantes,
            boolean visibleEnMenu
    ) {
        this.id = id;
        this.productName = productName;
        this.precio = precio;
        this.precioMinimo = precioMinimo;
        this.porcentajeDescuento = porcentajeDescuento;
        this.tienePromocion = tienePromocion;
        this.imageUrl = imageUrl;
        this.categoriaNombre = categoriaNombre;
        this.marcaNombre = marcaNombre;
        this.stockSimple = stockSimple;
        this.tieneVariantes = tieneVariantes;
        this.visibleEnMenu = visibleEnMenu;
    }
    
    public Integer getStockTotal() {

        if (tieneVariantes && variantes != null) {
            return variantes.stream()
                    .map(v -> v.getStock() != null ? v.getStock() : 0)
                    .reduce(0, Integer::sum);
        }

        return getStockSimple();
    }
    public BigDecimal getPrecioFinal() {
        if (!tienePromocion || porcentajeDescuento == null) return precio;

        BigDecimal descuento = precio
            .multiply(BigDecimal.valueOf(porcentajeDescuento))
            .divide(BigDecimal.valueOf(100));

        return precio.subtract(descuento);
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

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public boolean isTienePromocion() {
        return tienePromocion;
    }

    public void setTienePromocion(boolean tienePromocion) {
        this.tienePromocion = tienePromocion;
    }

    public String getImageUrl() {
        return imageUrl;
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

    public List<ProductoVarianteDTO> getVariantes() {
        return variantes;
    }

    public void setVariantes(List<ProductoVarianteDTO> variantes) {
        this.variantes = variantes;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }
}