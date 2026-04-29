package com.ecomerce.store.mapper;

import com.ecomerce.store.dto.producto.admin.ProductoAdminListDTO;
import com.ecomerce.store.dto.producto.shared.ProductoVarianteDTO;
import com.ecomerce.store.model.ImagenProducto;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.model.ProductoVariante;
import com.ecomerce.store.model.VarianteAtributo;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductoAdminListMapper {

    // =====================================
    // ENTITY -> DTO
    // =====================================
    public static ProductoAdminListDTO toDTO(Producto p) {

        ProductoAdminListDTO dto = new ProductoAdminListDTO();

        // =====================================
        // BÁSICO
        // =====================================
        dto.setId(p.getId());
        dto.setProductName(p.getProductName());

        dto.setPrecio(p.getPrice());
        dto.setPorcentajeDescuento(
                p.getPorcentajeDescuento() != null
                        ? p.getPorcentajeDescuento()
                        : 0.0
        );

        dto.setTienePromocion(
                Boolean.TRUE.equals(p.getTienePromocion())
        );

        dto.setVisibleEnMenu(p.isVisibleEnMenu());

        // =====================================
        // CATEGORÍA / MARCA
        // =====================================
        dto.setCategoriaId(
                p.getCategoria() != null
                        ? p.getCategoria().getId()
                        : null
        );

        dto.setCategoriaNombre(
                p.getCategoria() != null
                        ? p.getCategoria().getNombre()
                        : null
        );

        dto.setMarcaNombre(
                p.getMarca() != null
                        ? p.getMarca().getNombre()
                        : null
        );

        // =====================================
        // STOCK SIMPLE
        // =====================================
        dto.setStockSimple(
                p.getStockSimple() != null
                        ? p.getStockSimple()
                        : 0
        );

        // =====================================
        // VARIANTES
        // =====================================
        boolean tieneVariantes =
                p.getVariantes() != null
                        && !p.getVariantes().isEmpty();

        dto.setTieneVariantes(tieneVariantes);

        if (tieneVariantes) {

            List<ProductoVarianteDTO> variantes =
                    p.getVariantes()
                            .stream()
                            .map(ProductoAdminListMapper::mapVariante)
                            .toList();

            dto.setVariantes(variantes);

            // precio mínimo entre variantes
            BigDecimal min = p.getVariantes()
                    .stream()
                    .map(ProductoVariante::getPrecio)
                    .filter(pr -> pr != null)
                    .min(Comparator.naturalOrder())
                    .orElse(p.getPrice());

            dto.setPrecioMinimo(min);

        } else {
            dto.setPrecioMinimo(p.getPrice());
        }

        // =====================================
        // IMAGEN PRINCIPAL
        // =====================================
        String imagen = p.getImagenes()
                .stream()
                .sorted(
                        Comparator.comparing(
                                ImagenProducto::getPrincipal,
                                Comparator.nullsLast(Boolean::compareTo)
                        ).reversed()
                                .thenComparing(ImagenProducto::getOrden)
                )
                .map(ImagenProducto::getImageUrl)
                .findFirst()
                .orElse("/img/default.png");

        dto.setImageUrl(imagen);

        return dto;
    }

    // =====================================
    // VARIANTE MAPPER
    // =====================================
    private static ProductoVarianteDTO mapVariante(ProductoVariante v) {

        ProductoVarianteDTO dto = new ProductoVarianteDTO();

        dto.setId(v.getId());
        dto.setPrecio(v.getPrecio());
        dto.setStock(v.getStock());

        if (v.getAtributos() != null) {

            Map<String, String> attrs =
                    v.getAtributos()
                            .stream()
                            .collect(Collectors.toMap(
                                    VarianteAtributo::getNombre,
                                    VarianteAtributo::getValor,
                                    (a, b) -> a
                            ));

            dto.setAtributos(attrs);
        }

        return dto;
    }
}