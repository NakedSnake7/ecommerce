package com.ecomerce.store.mapper;

import java.math.BigDecimal; 
import java.util.Comparator;

import com.ecomerce.store.dto.producto.publico.ProductoCardDTO;
import com.ecomerce.store.model.ImagenProducto;
import com.ecomerce.store.model.Producto;

public class ProductoCardMapper {

	public static ProductoCardDTO toDTO(Producto p) {

	    ProductoCardDTO dto = new ProductoCardDTO();

	    dto.setId(p.getId());
	    dto.setProductName(p.getProductName());

	    dto.setCategoriaId(
	        p.getCategoria() != null ? p.getCategoria().getId() : null
	    );

	    dto.setCategoriaNombre(
	        p.getCategoria() != null ? p.getCategoria().getNombre() : null
	    );

	    dto.setMarcaNombre(
	        p.getMarca() != null ? p.getMarca().getNombre() : null
	    );

	    // ==============================
	    // IMAGEN PRINCIPAL
	    // ==============================
	    dto.setImageUrl(
	        p.getImagenes() != null && !p.getImagenes().isEmpty()
	            ? p.getImagenes().stream()
	                .sorted(
	                    Comparator.comparing(
	                        ImagenProducto::getPrincipal,
	                        Comparator.nullsLast(Boolean::compareTo)
	                    ).reversed()
	                    .thenComparing(ImagenProducto::getOrden)
	                )
	                .map(ImagenProducto::getImageUrl)
	                .findFirst()
	                .orElse("/img/default.png")
	            : "/img/default.png"
	    );

	    // ==============================
	    // VARIANTES
	    // ==============================
	    boolean tieneVariantes =
	        p.getVariantes() != null && !p.getVariantes().isEmpty();

	    dto.setTieneVariantes(tieneVariantes);

	    // ==============================
	    // STOCK
	    // ==============================
	    dto.setStockSimple(
	        p.getStockSimple() != null ? p.getStockSimple() : 0
	    );

	    // ==============================
	    // PRECIO BASE
	    // ==============================
	    dto.setPrecio(p.getPrice());

	    // ==============================
	    // PRECIO MÍNIMO (VARIANTES)
	    // ==============================
	    if (tieneVariantes) {

	        var min = p.getVariantes().stream()
	            .map(v -> v.getPrecio())
	            .filter(java.util.Objects::nonNull)
	            .min(BigDecimal::compareTo)
	            .orElse(p.getPrice());

	        dto.setPrecioMinimo(min);
	    } else {
	        dto.setPrecioMinimo(p.getPrice());
	    }

	    // ==============================
	    // PROMOCIÓN
	    // ==============================
	    dto.setPorcentajeDescuento(
	        p.getPorcentajeDescuento() != null
	            ? p.getPorcentajeDescuento()
	            : 0.0
	    );

	    dto.setTienePromocion(
	        p.getPorcentajeDescuento() != null &&
	        p.getPorcentajeDescuento() > 0
	    );

	    dto.setVisibleEnMenu(p.isVisibleEnMenu());

	    return dto;
	}
}