package com.ecomerce.store.mapper;

import com.ecomerce.store.dto.ProductoDTO;
import com.ecomerce.store.dto.ProductoVarianteDTO;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.model.ProductoVariante;
import com.ecomerce.store.model.ImagenProducto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductoMapper {

    public static ProductoDTO toDTO(Producto producto) {

        ProductoDTO dto = new ProductoDTO();

        dto.setId(producto.getId());
        dto.setProductName(producto.getProductName());
        dto.setDescription(producto.getDescription());

        // =========================
        // IMAGEN
        // =========================
        String imagen = (producto.getImagenes() != null && !producto.getImagenes().isEmpty())
                ? producto.getImagenes().stream()
                    .map(ImagenProducto::getImageUrl)
                    .findFirst()
                    .orElse("/imgs/default.png")
                : "/imgs/default.png";

        dto.setImageUrl(imagen);

        // =========================
        // CATEGORIA
        // =========================
        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        } else {
            dto.setCategoriaNombre("Sin categoría");
        }

        dto.setTienePromocion(producto.getTienePromocion());
        dto.setPorcentajeDescuento(producto.getPorcentajeDescuento());
        dto.setVisibleEnMenu(producto.isVisibleEnMenu());

        // =========================
        // VARIANTES
        // =========================
        List<ProductoVarianteDTO> variantesDTO = mapVariantes(producto);
        dto.setVariantes(variantesDTO);

        boolean tieneVariantes = !variantesDTO.isEmpty();

        // =========================
        // PRECIO BASE
        // =========================
        ProductoVariante variante = getVariantePrincipal(producto);

        BigDecimal precioBase = (variante != null && variante.getPrecio() != null)
                ? variante.getPrecio()
                : producto.getPrice();

        dto.setPrice(precioBase);

        // =========================
        // PRECIO FINAL (DESCUENTO)
        // =========================
        BigDecimal precioFinal = precioBase;

        if (Boolean.TRUE.equals(producto.getTienePromocion())
                && producto.getPorcentajeDescuento() != null
                && producto.getPorcentajeDescuento() > 0) {

            BigDecimal descuento = precioBase
                    .multiply(BigDecimal.valueOf(producto.getPorcentajeDescuento()))
                    .divide(BigDecimal.valueOf(100));

            precioFinal = precioBase.subtract(descuento);
        }

        dto.setPrecioFinal(precioFinal);

        // =========================
        // PRECIO MINIMO
        // =========================
        if (tieneVariantes) {
            BigDecimal min = producto.getVariantes().stream()
                    .map(v -> v.getPrecio() != null ? v.getPrecio() : producto.getPrice())
                    .min(BigDecimal::compareTo)
                    .orElse(producto.getPrice());

            dto.setPrecioMinimo(min);
        } else {
            dto.setPrecioMinimo(precioFinal);
        }

        // =========================
        // STOCK TOTAL
        // =========================
        int stockTotal = (producto.getVariantes() != null)
                ? producto.getVariantes()
                    .stream()
                    .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                    .sum()
                : 0;

        dto.setStockTotal(stockTotal);

        return dto;
    }

    // =========================
    // VARIANTES LIST
    // =========================
    private static List<ProductoVarianteDTO> mapVariantes(Producto producto) {

        if (producto.getVariantes() == null || producto.getVariantes().isEmpty()) {
            return List.of();
        }

        return producto.getVariantes()
                .stream()
                .map(ProductoMapper::toVarianteDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // VARIANTE → DTO
    // =========================
    private static ProductoVarianteDTO toVarianteDTO(ProductoVariante v) {

        ProductoVarianteDTO dto = new ProductoVarianteDTO();

        dto.setId(v.getId());
        dto.setNombre(v.getNombreVisual());
        dto.setPrecio(v.getPrecioFinal());
        dto.setStock(v.getStock() != null ? v.getStock() : 0);

        Map<String, String> atributosMap = v.getAtributos()
                .stream()
                .collect(Collectors.toMap(
                        attr -> attr.getNombre(),
                        attr -> attr.getValor()
                ));

        dto.setAtributos(atributosMap);

        return dto;
    }

    // =========================
    // VARIANTE PRINCIPAL
    // =========================
    private static ProductoVariante getVariantePrincipal(Producto producto) {

        if (producto.getVariantes() == null || producto.getVariantes().isEmpty()) {
            return null;
        }

        return producto.getVariantes()
                .stream()
                .filter(v -> Boolean.TRUE.equals(v.getPrincipal()))
                .findFirst()
                .orElse(producto.getVariantes().get(0));
    }
}