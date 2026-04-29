package com.ecomerce.store.controller;

import com.ecomerce.store.dto.producto.admin.ProductoAdminDTO;
import com.ecomerce.store.dto.producto.publico.ProductoCardDTO;
import com.ecomerce.store.dto.producto.publico.ProductoDetailDTO;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.service.CategoriaService;
import com.ecomerce.store.service.ProductoService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    // ==================================================
    // LISTADO
    // ==================================================
    @GetMapping
    public List<ProductoCardDTO> listarProductos() {
        return productoService.obtenerProductosCompletos();
    }

    // ==================================================
    // OBTENER POR ID
    // ==================================================
    @GetMapping("/{id}")
    public ProductoDetailDTO obtenerProducto(@PathVariable Long id) {
        return productoService.obtenerDetalleProducto(id);
    }

    // ==================================================
    // ELIMINAR PRODUCTO
    // ==================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // ==================================================
    // EDITAR PRODUCTO (AJAX)
    // ==================================================
    @PostMapping("/editar/{id}")
    public Map<String, Object> actualizarProducto(
            @PathVariable Long id,
            @ModelAttribute ProductoAdminDTO dto,
            @RequestParam(value = "imagenes", required = false)
            List<MultipartFile> nuevasImagenes,

            @RequestParam(value = "eliminarImagenes", required = false)
            List<Long> eliminarImagenes
    ) {

        Producto datos = new Producto();

        datos.setProductName(dto.getProductName());
        datos.setPrice(dto.getPrecio());
        datos.setDescription(dto.getDescription());
        datos.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        datos.setStockSimple(dto.getStockSimple());

        // Categoría
        if (dto.getNuevaCategoria() != null &&
            !dto.getNuevaCategoria().isBlank()) {

            datos.setCategoria(
                categoriaService.obtenerOCrearCategoria(
                    dto.getNuevaCategoria()
                )
            );

        } else if (dto.getCategoriaId() != null) {

            datos.setCategoria(
                categoriaService.obtenerPorId(
                    dto.getCategoriaId()
                )
            );

        } else {
            throw new IllegalArgumentException(
                "Debe seleccionar categoría"
            );
        }

        Producto actualizado =
            productoService.actualizarProductoCompleto(
                id,
                datos,
                nuevasImagenes,
                eliminarImagenes,
                dto.getVariantes()
            );

        return Map.of(
            "success", true,
            "productoId", actualizado.getId()
        );
    }

    // ==================================================
    // ELIMINAR IMAGEN INMEDIATA (AJAX)
    // ==================================================
    @DeleteMapping("/{productoId}/eliminar-imagen/{idImagen}")
    public ResponseEntity<Void> eliminarImagenInmediato(
            @PathVariable Long productoId,
            @PathVariable Long idImagen
    ) {

        productoService.eliminarImagenInmediatoSeguro(productoId, idImagen);
        return ResponseEntity.noContent().build();
    }


    // ==================================================
    // TOGGLES
    // ==================================================
    @PostMapping("/togglePromocion")
    public Boolean togglePromocion(@RequestParam Long productoId) {
        return productoService.togglePromocion(productoId);
    }

    @PostMapping("/toggleVisibility")
    public Boolean toggleVisibility(@RequestParam Long productoId) {
        return productoService.toggleVisibility(productoId);
    }
    @PostMapping("/toggleCategoria")
    public ResponseEntity<Void> toggleCategoria(
            @RequestParam Long categoriaId,
            @RequestParam boolean visible
    ) {
        productoService.toggleVisibilidadPorCategoria(categoriaId, visible);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/actualizarPrecio")
    public ResponseEntity<?> actualizarPrecio(
            @RequestParam Long productoId,
            @RequestParam BigDecimal precio
    ) {
        productoService.actualizarPrecio(productoId, precio);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/ajustarPrecioCategoria")
    public ResponseEntity<Void> ajustarPrecioCategoria(
            @RequestParam Long categoriaId,
            @RequestParam BigDecimal valor,
            @RequestParam String modo
    ) {
        productoService.ajustarPrecioCategoria(categoriaId, valor, modo);
        return ResponseEntity.ok().build();
    }


}
