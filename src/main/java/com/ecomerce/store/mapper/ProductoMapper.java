package com.ecomerce.store.mapper;

import com.ecomerce.store.dto.ProductoDTO;
import com.ecomerce.store.dto.ProductoVarianteDTO;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.model.ProductoVariante;
import com.ecomerce.store.model.ImagenProducto;

import java.util.List;
import java.util.stream.Collectors;

public class ProductoMapper {

    public static ProductoDTO toDTO(Producto producto) {

        ProductoDTO dto = new ProductoDTO();

        dto.setId(producto.getId());
        dto.setProductName(producto.getProductName());
        dto.setDescription(producto.getDescription());

        // =========================
        // 🔥 IMAGEN PRINCIPAL (FIX REAL)
        // =========================
        String imagen = producto.getImagenes() != null && !producto.getImagenes().isEmpty()
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
         dto.setCategoriaId(null);
         dto.setCategoriaNombre("Sin categoría");
     }

        dto.setTienePromocion(producto.getTienePromocion());
        dto.setPorcentajeDescuento(producto.getPorcentajeDescuento());
        dto.setVisibleEnMenu(producto.isVisibleEnMenu());

        // =========================
        // 🔥 PRECIO DESDE VARIANTE
        // =========================
        ProductoVariante variante = getVariantePrincipal(producto);

        if (variante != null && variante.getPrecio() != null) {
            dto.setPrice(variante.getPrecio());
        } else {
            dto.setPrice(producto.getPrice());
        }

        // =========================
        // 🔥 STOCK TOTAL (FIX CLAVE)
        // =========================
        int stockTotal = producto.getVariantes() != null
                ? producto.getVariantes()
                    .stream()
                    .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                    .sum()
                : 0;

        dto.setStockTotal(stockTotal);

        // =========================
        // VARIANTES
        // =========================
        dto.setVariantes(mapVariantes(producto));

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
                .orElse(
                    producto.getVariantes()
                            .stream()
                            .findFirst()
                            .orElse(null)
                );
    }
}