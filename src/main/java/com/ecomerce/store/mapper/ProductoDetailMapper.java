package com.ecomerce.store.mapper;

import com.ecomerce.store.dto.producto.publico.ProductoDetailDTO;
import com.ecomerce.store.dto.producto.shared.ProductoVarianteDTO;
import com.ecomerce.store.model.ImagenProducto;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.model.ProductoVariante;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ProductoDetailMapper {

    private static final String IMG_DEFAULT = "/imgs/default.png";
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private ProductoDetailMapper() {}

    public static ProductoDetailDTO toDTO(Producto p) {

        ProductoDetailDTO dto = new ProductoDetailDTO();

        // =========================
        // BASE
        // =========================
        dto.setId(p.getId());
        dto.setProductName(p.getProductName());
        dto.setDescription(p.getDescription());

        dto.setVisibleEnMenu(p.isVisibleEnMenu());

        // =========================
        // CATEGORIA / MARCA
        // =========================
        if (p.getCategoria() != null) {
            dto.setCategoriaId(p.getCategoria().getId());
            dto.setCategoriaNombre(p.getCategoria().getNombre());
        }

        if (p.getMarca() != null) {
            dto.setMarcaId(p.getMarca().getId());
            dto.setMarcaNombre(p.getMarca().getNombre());
        }

        // =========================
        // IMAGENES
        // =========================
        dto.setImagenes(
        	    p.getImagenes() != null && !p.getImagenes().isEmpty()
        	        ? p.getImagenes().stream()
        	            .map(ImagenProducto::getImageUrl)
        	            .filter(Objects::nonNull)
        	            .toList()
        	        : List.of(IMG_DEFAULT)
        	);

        	dto.setImageUrl(dto.getImagenes().get(0));

        // =========================
        // VARIANTES
        // =========================
        List<ProductoVarianteDTO> variantes = mapVariantes(p);
        dto.setVariantes(variantes);

        boolean tieneVariantes = !variantes.isEmpty();

        // =========================
        // STOCK
        // =========================
        dto.setStockSimple(calcularStock(p));

        // =========================
        // PRECIOS BASE
        // =========================
        BigDecimal precioBase = nvl(p.getPrice());

        BigDecimal precioMinimo = tieneVariantes
                ? variantes.stream()
                    .map(v -> nvl(v.getPrecio(), precioBase))
                    .min(Comparator.naturalOrder())
                    .orElse(precioBase)
                : precioBase;

        dto.setPrecio(precioBase);
        dto.setPrecioMinimo(scale(precioMinimo));

        // =========================
        // PROMOCION
        // =========================
        dto.setTienePromocion(Boolean.TRUE.equals(p.getTienePromocion()));
        dto.setPorcentajeDescuento(p.getPorcentajeDescuento());

        // =========================
        // PRECIO FINAL
        // =========================
        BigDecimal base = tieneVariantes ? precioMinimo : precioBase;

        BigDecimal precioFinal = dto.isTienePromocion()
                && dto.getPorcentajeDescuento() != null
                && dto.getPorcentajeDescuento() > 0
                ? aplicarDescuento(base, dto.getPorcentajeDescuento())
                : base;

        dto.setPrecioConDescuento(scale(precioFinal));

        return dto;
    }

    // =========================
    // VARIANTES
    // =========================
    private static List<ProductoVarianteDTO> mapVariantes(Producto p) {

        if (p.getVariantes() == null || p.getVariantes().isEmpty()) {
            return List.of();
        }

        return p.getVariantes()
                .stream()
                .map(ProductoDetailMapper::toVarianteDTO)
                .toList();
    }

    private static ProductoVarianteDTO toVarianteDTO(ProductoVariante v) {

        ProductoVarianteDTO dto = new ProductoVarianteDTO();

        dto.setId(v.getId());
        dto.setNombre(v.getNombreVisual());
        dto.setStock(v.getStock() != null ? v.getStock() : 0);
        dto.setPrecio(scale(v.getPrecioFinal()));

        Map<String, String> atributos = v.getAtributos()
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getNombre(),
                        a -> a.getValor(),
                        (a, b) -> a
                ));

        dto.setAtributos(atributos);

        return dto;
    }

    // =========================
    // STOCK
    // =========================
    private static int calcularStock(Producto p) {

        if (p.getVariantes() == null || p.getVariantes().isEmpty()) {
            return p.getStockSimple() != null ? p.getStockSimple() : 0;
        }

        return p.getVariantes()
                .stream()
                .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                .sum();
    }

    // =========================
    // HELPERS
    // =========================
    private static BigDecimal aplicarDescuento(BigDecimal precio, Double porcentaje) {

        BigDecimal descuento = precio
                .multiply(BigDecimal.valueOf(porcentaje))
                .divide(CIEN, 2, RoundingMode.HALF_UP);

        return precio.subtract(descuento);
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal nvl(BigDecimal v, BigDecimal fallback) {
        return v != null ? v : fallback;
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}