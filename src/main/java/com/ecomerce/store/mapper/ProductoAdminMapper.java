package com.ecomerce.store.mapper;

import com.ecomerce.store.dto.producto.admin.ProductoAdminDTO;
import com.ecomerce.store.dto.producto.shared.ProductoVarianteDTO;
import com.ecomerce.store.model.Producto;

import java.math.BigDecimal;
import java.util.List;

public final class ProductoAdminMapper {

    private ProductoAdminMapper() {}

    public static ProductoAdminDTO toDTO(Producto p) {

        ProductoAdminDTO dto = new ProductoAdminDTO();

        // =========================
        // BASE
        // =========================
        dto.setId(p.getId());
        dto.setProductName(p.getProductName());
        dto.setDescription(p.getDescription());
        dto.setPrecio(
                p.getPrice() != null
                        ? p.getPrice()
                        : BigDecimal.ZERO
        );

        // =========================
        // RELACIONES
        // =========================
        if (p.getCategoria() != null) {
            dto.setCategoriaId(
                    p.getCategoria().getId()
            );
        }

        if (p.getMarca() != null) {
            dto.setMarcaId(
                    p.getMarca().getId()
            );
        }

        // =========================
        // CONFIG
        // =========================
        dto.setVisibleEnMenu(
                p.isVisibleEnMenu()
        );

        dto.setTienePromocion(
                Boolean.TRUE.equals(
                        p.getTienePromocion()
                )
        );

        dto.setPorcentajeDescuento(
                p.getPorcentajeDescuento() != null
                        ? p.getPorcentajeDescuento()
                        : 0.0
        );

        dto.setStockSimple(
                p.getStockSimple() != null
                        ? p.getStockSimple()
                        : 0
        );

        // =========================
        // VARIANTES
        // =========================
        List<ProductoVarianteDTO> variantes =
                p.getVariantes() != null
                ? p.getVariantes()
                    .stream()
                    .map(v -> {

                        ProductoVarianteDTO dtoV =
                                new ProductoVarianteDTO();

                        dtoV.setId(v.getId());
                        dtoV.setNombre(v.getNombreVisual());

                        dtoV.setStock(
                                v.getStock() != null
                                        ? v.getStock()
                                        : 0
                        );

                        dtoV.setPrecio(
                                v.getPrecioFinal()
                        );

                        dtoV.setAtributos(
                                v.getAtributosMap()
                        );

                        return dtoV;
                    })
                    .toList()
                : List.of();

        dto.setVariantes(variantes);

        // =========================
        // IMAGENES
        // =========================
        if (p.getImagenes() != null) {

            dto.setImagenesExistentes(
                    p.getImagenes()
                            .stream()
                            .map(img -> img.getId())
                            .toList()
            );

            dto.setUrlsImagenesExistentes(
                    p.getImagenes()
                            .stream()
                            .map(img -> img.getImageUrl())
                            .toList()
            );
        }

        return dto;
    }
}