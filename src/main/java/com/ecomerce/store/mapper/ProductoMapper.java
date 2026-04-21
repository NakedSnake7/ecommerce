package com.ecomerce.store.mapper;

import com.ecomerce.store.dto.ProductoDTO;
import com.ecomerce.store.dto.ProductoVarianteDTO;
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

public final class ProductoMapper {

    private static final String IMG_DEFAULT = "/imgs/default.png";
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private ProductoMapper() {
    }

    // =========================================================
    // MAIN MAPPER
    // =========================================================
    public static ProductoDTO toDTO(Producto producto) {

        ProductoDTO dto = new ProductoDTO();

        // =========================
        // DATOS BASE
        // =========================
        dto.setId(producto.getId());
        dto.setProductName(producto.getProductName());
        dto.setDescription(producto.getDescription());
        dto.setVisibleEnMenu(producto.isVisibleEnMenu());
        dto.setTienePromocion(Boolean.TRUE.equals(producto.getTienePromocion()));
        dto.setPorcentajeDescuento(producto.getPorcentajeDescuento());

        // =========================
        // IMAGEN
        // =========================
        dto.setImageUrl(obtenerImagenPrincipal(producto));
        
        if (producto.getImagenes() != null) {

            producto.getImagenes().forEach(img -> {
                dto.getImagenesExistentes().add(img.getId());
                dto.getUrlsImagenesExistentes().add(img.getImageUrl());
            });

        }

        // =========================
        // CATEGORIA
        // =========================
        mapCategoria(producto, dto);

        // =========================
        // VARIANTES
        // =========================
        List<ProductoVarianteDTO> variantes = mapVariantes(producto);
        dto.setVariantes(variantes);

        boolean tieneVariantes = !variantes.isEmpty();

        // =========================
        // PRECIO BASE REAL PRODUCTO
        // =========================
        BigDecimal precioBase = nvl(producto.getPrice());
        dto.setPrice(precioBase);

        // =========================
        // PRECIO MÍNIMO / MÁXIMO
        // =========================
        BigDecimal precioMinimo = precioBase;
        BigDecimal precioMaximo = precioBase;

        if (tieneVariantes) {

            List<BigDecimal> precios = producto.getVariantes().stream()
                    .map(v -> nvl(v.getPrecio(), precioBase))
                    .toList();

            precioMinimo = precios.stream()
                    .min(Comparator.naturalOrder())
                    .orElse(precioBase);

            precioMaximo = precios.stream()
                    .max(Comparator.naturalOrder())
                    .orElse(precioBase);
        }

        dto.setPrecioMinimo(scale(precioMinimo));

        // si tu DTO tiene setter:
        // dto.setPrecioMaximo(scale(precioMaximo));

        // =========================
        // PRECIO FINAL CON DESCUENTO
        // =========================
        BigDecimal precioFinal;

        if (dto.getTienePromocion()
                && dto.getPorcentajeDescuento() != null
                && dto.getPorcentajeDescuento() > 0) {

            BigDecimal basePromo = tieneVariantes
                    ? precioMinimo
                    : precioBase;

            precioFinal = aplicarDescuento(
                    basePromo,
                    dto.getPorcentajeDescuento()
            );

        } else {
            precioFinal = tieneVariantes
                    ? precioMinimo
                    : precioBase;
        }

        dto.setPrecioFinal(scale(precioFinal));

        // =========================
        // STOCK TOTAL
        // =========================
        dto.setStockSimple(calcularStock(producto));
        
        return dto;
    }

    // =========================================================
    // VARIANTES
    // =========================================================
    private static List<ProductoVarianteDTO> mapVariantes(Producto producto) {

        if (producto.getVariantes() == null || producto.getVariantes().isEmpty()) {
            return List.of();
        }

        return producto.getVariantes().stream()
                .map(ProductoMapper::toVarianteDTO)
                .toList();
    }

    private static ProductoVarianteDTO toVarianteDTO(ProductoVariante variante) {

        ProductoVarianteDTO dto = new ProductoVarianteDTO();

        dto.setId(variante.getId());
        dto.setNombre(variante.getNombreVisual());
        dto.setStock(variante.getStock() != null ? variante.getStock() : 0);
        dto.setPrecio(scale(variante.getPrecioFinal()));

        Map<String, String> atributos = variante.getAtributos()
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getNombre(),
                        a -> a.getValor(),
                        (a, b) -> a
                ));

        dto.setAtributos(atributos);

        return dto;
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private static void mapCategoria(Producto producto, ProductoDTO dto) {

        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        } else {
            dto.setCategoriaNombre("Sin categoría");
        }
    }

    private static String obtenerImagenPrincipal(Producto producto) {

        if (producto.getImagenes() == null || producto.getImagenes().isEmpty()) {
            return IMG_DEFAULT;
        }

        return producto.getImagenes().stream()
                .map(ImagenProducto::getImageUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(IMG_DEFAULT);
    }

    private static int calcularStock(Producto producto) {

        // Producto simple
        if (producto.getVariantes() == null || producto.getVariantes().isEmpty()) {
            return producto.getStockSimple() != null
                    ? producto.getStockSimple()
                    : 0;
        }

        // Producto con variantes
        return producto.getVariantes().stream()
                .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                .sum();
    }

    private static BigDecimal aplicarDescuento(
            BigDecimal precio,
            Double porcentaje
    ) {

        BigDecimal descuento = precio
                .multiply(BigDecimal.valueOf(porcentaje))
                .divide(CIEN, 2, RoundingMode.HALF_UP);

        return precio.subtract(descuento);
    }

    private static BigDecimal nvl(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private static BigDecimal nvl(BigDecimal valor, BigDecimal fallback) {
        return valor != null ? valor : fallback;
    }

    private static BigDecimal scale(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}