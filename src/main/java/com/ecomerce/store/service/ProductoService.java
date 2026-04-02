package com.ecomerce.store.service;

import com.ecomerce.store.dto.CategoriaGrupoDTO;   

import com.ecomerce.store.dto.CloudinaryUploadResult;
import com.ecomerce.store.dto.ProductoDTO;
import com.ecomerce.store.dto.ProductoResumenDTO;
import com.ecomerce.store.exception.ImageUploadException;
import com.ecomerce.store.mapper.ProductoMapper;
import com.ecomerce.store.model.ImagenProducto;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.repository.ImagenProductoRepository;
import com.ecomerce.store.repository.ProductoRepository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductoService {

	private static final Logger log =
            LoggerFactory.getLogger(ProductoService.class);
    @Autowired
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           ImagenProductoRepository imagenProductoRepository,
                           CloudinaryService cloudinaryService) {
        this.productoRepository = productoRepository;
        this.imagenProductoRepository = imagenProductoRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ============================================================
    // OBTENCIONES
    // ============================================================

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findProductosVisiblesConTodo();
    }
    
    
    
    public List<Producto> obtenerProductosVisiblesConTodo() {
        return productoRepository.findProductosVisiblesConTodo();
    }

    public List<String> obtenerCategorias() {
        return productoRepository.obtenerNombresCategoriasVisibles();
    }

    @Transactional
    public Producto obtenerProducto(Long id) {
        return productoRepository.findByIdConTodo(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    public Optional<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByProductName(nombre);
    }

    
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerProductosCompletos(){

        List<Producto> productos = productoRepository.findProductosVisiblesConTodo();

        return productos.stream()
                .map(ProductoMapper::toDTO)
                .toList();
    }
    
    
    @Transactional
    public Producto guardarProducto(Producto producto) {
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = obtenerProducto(id);
        eliminarImagenesPorProducto(producto);
        productoRepository.delete(producto);
    }

    // ============================================================
    // TOGGLES
    // ============================================================

    @Transactional
    public boolean toggleVisibility(Long id) {
        Producto producto = obtenerProducto(id);
        producto.setVisibleEnMenu(!producto.isVisibleEnMenu());
        productoRepository.save(producto);
        return producto.isVisibleEnMenu();
    }

    @Transactional
    public boolean togglePromocion(Long id) {
        Producto producto = obtenerProducto(id);
        producto.setTienePromocion(!Boolean.TRUE.equals(producto.getTienePromocion()));
        productoRepository.save(producto);
        return producto.getTienePromocion();
    }

    // ============================================================
    // ACTUALIZACIÓN COMPLETA (🔥 MÉTODO CENTRAL 🔥)
    // ============================================================

    @Transactional
    public Producto actualizarProductoCompleto(
            Long productoId,
            Producto datos,
            List<MultipartFile> nuevasImagenes,
            List<Long> eliminarImagenes
    ) {

        Producto producto = obtenerProducto(productoId);

        producto.setProductName(datos.getProductName());
        producto.setPrice(datos.getPrice());
        producto.setDescription(datos.getDescription());
        producto.setPorcentajeDescuento(datos.getPorcentajeDescuento());
        producto.setCategoria(datos.getCategoria());

        validarProducto(producto);

        List<String> publicIdsSubidos = new ArrayList<>();

        try {
            subirImagenesInterno(producto, nuevasImagenes, publicIdsSubidos);
            eliminarImagenesInterno(productoId, eliminarImagenes);
            
            log.info("Actualizando producto id={}", productoId);

            productoRepository.save(producto);
            return producto;

        } catch (Exception e) {
            for (String publicId : publicIdsSubidos) {
                try {
                    cloudinaryService.eliminarImagen(publicId);
                } catch (Exception ignored) {}
            }
            throw e;
        }
    }

    // ============================================================
    // MÉTODOS INTERNOS
    // ============================================================

    private void subirImagenesInterno(
            Producto producto,
            List<MultipartFile> nuevasImagenes,
            List<String> publicIdsSubidos
    ) {

        if (nuevasImagenes == null) return;

        List<String> tiposPermitidos = List.of("image/jpeg", "image/png", "image/webp");

        for (MultipartFile archivo : nuevasImagenes) {

            if (archivo.isEmpty()) continue;

            String contentType = archivo.getContentType();
            if (contentType == null || !tiposPermitidos.contains(contentType)) {
                throw new IllegalArgumentException("Formato no permitido");
            }
            log.info("Subiendo imagen. productoId={}", producto.getId());

            CloudinaryUploadResult result;
            try {
                result = cloudinaryService.subirImagen(archivo);
            } catch (IOException e) {
                throw new ImageUploadException(
                        "Error al subir la imagen. Intenta nuevamente.",
                        e
                );
            }

            publicIdsSubidos.add(result.getPublicId());

            ImagenProducto imagen = new ImagenProducto(
                    result.getSecureUrl(),
                    result.getPublicId(),
                    producto
            );

            imagenProductoRepository.save(imagen);
        }
    }

    private void eliminarImagenesInterno(Long productoId, List<Long> ids) {

        if (ids == null || ids.isEmpty()) return;

        List<ImagenProducto> imagenes = imagenProductoRepository.findAllById(ids);

        for (ImagenProducto img : imagenes) {

            if (!img.getProducto().getId().equals(productoId)) {
                throw new IllegalArgumentException("Imagen no pertenece al producto");
            }

            cloudinaryService.eliminarImagen(img.getPublicId());
            imagenProductoRepository.delete(img);
        }
    }
    
 

    // ============================================================
    // LEGACY
    // ============================================================

    @Transactional
    public void eliminarImagenInmediatoSeguro(Long productoId, Long idImagen) {

        ImagenProducto img = imagenProductoRepository.findById(idImagen)
                .orElseThrow(() -> new ProductoNotFoundException(idImagen));

        // 🔒 VALIDAR OWNERSHIP
        if (!img.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException(
                    "La imagen no pertenece al producto indicado"
            );
        }

        // Eliminar de Cloudinary
        cloudinaryService.eliminarImagen(img.getPublicId());

        // Eliminar de BD
        imagenProductoRepository.delete(img);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void eliminarImagenesPorProducto(Producto producto) {

        List<ImagenProducto> imagenes =
                imagenProductoRepository.findByProductoId(producto.getId());

        for (ImagenProducto img : imagenes) {
            cloudinaryService.eliminarImagen(img.getPublicId());
        }

        imagenProductoRepository.deleteAllInBatch(imagenes);
    }

    private void validarProducto(Producto producto) {

        if (producto.getProductName() == null || producto.getProductName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (producto.getPrice() == null || producto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Precio inválido");
        }
        

        if (producto.getVariantes() != null && !producto.getVariantes().isEmpty()) {

            boolean hayStockNegativo = producto.getVariantes().stream()
                    .anyMatch(v -> v.getStock() != null && v.getStock() < 0);

            if (hayStockNegativo) {
                throw new IllegalArgumentException("Stock inválido en variantes");
            }
        }
        
        if (!producto.tieneVariantes()) {
            throw new IllegalArgumentException(
                "Todos los productos deben tener al menos una variante"
            );
        }
    }

    // ============================================================
    // EXCEPCIÓN
    // ============================================================

    public static class ProductoNotFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ProductoNotFoundException(Long id) {
            super("Producto no encontrado con ID: " + id);
        }
    }
   
    @Transactional(readOnly = true)
    public List<CategoriaGrupoDTO> obtenerProductosAgrupadosPorCategoria() {

    	List<Producto> productos = productoRepository.findProductosVisiblesConTodo();
        return productos.stream()
            .map(ProductoMapper::toDTO) // 🔥 aquí ocurre todo dentro de TX
            .collect(Collectors.groupingBy(
                p -> p.getCategoriaNombre() != null
                        ? p.getCategoriaNombre()
                        : "Sin categoría",
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet()
            .stream()
            .map(e -> new CategoriaGrupoDTO(e.getKey(), e.getValue()))
            .toList();
    }

    @Transactional
    public void toggleVisibilidadPorCategoria(Long categoriaId, boolean visible) {

        productoRepository.updateVisibilidadPorCategoria(categoriaId, visible);
    }
    
    @Transactional
    public void actualizarPrecio(Long id, BigDecimal precio) {
    	if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
    	    throw new IllegalArgumentException("Precio inválido");
    	}

    	Producto producto = productoRepository.findById(id)
    	        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

    	producto.setPrice(precio.setScale(2, RoundingMode.HALF_UP));
    	if (!producto.getVariantes().isEmpty()) {
    	    throw new IllegalStateException("Producto con variantes no usa precio base");
    	}
    }
    @Transactional
    public void ajustarPrecioCategoria(
            Long categoriaId,
            BigDecimal valor,
            String modo
    ) {

        List<Producto> productos =
                productoRepository.findByCategoriaId(categoriaId);

        if (productos.isEmpty()) {
            throw new RuntimeException("No hay productos en la categoría");
        }

        for (Producto p : productos) {

            BigDecimal precioActual = p.getPrice();
            BigDecimal nuevoPrecio;

            if ("MONTO".equalsIgnoreCase(modo)) {

                nuevoPrecio = precioActual.add(valor);

            } else if ("PORCENTAJE".equalsIgnoreCase(modo)) {

                BigDecimal ajuste = precioActual
                        .multiply(valor)
                        .divide(BigDecimal.valueOf(100));

                nuevoPrecio = precioActual.add(ajuste);

            } else {
                throw new IllegalArgumentException("Modo inválido");
            }

            if (nuevoPrecio.compareTo(BigDecimal.ZERO) < 0) {
                nuevoPrecio = BigDecimal.ZERO;
            }

            if (!p.tieneVariantes() && p.getPrice() != null) {
                p.setPrice(nuevoPrecio.setScale(2, RoundingMode.HALF_UP));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ProductoResumenDTO> obtenerProductosIndexOptimizado() {

        List<Producto> productos = productoRepository.findProductosVisiblesConTodo();

        return productos.stream().map(p -> new ProductoResumenDTO(
                p.getId(),
                p.getProductName(),
                p.getPrice(),
                p.getTienePromocion(),
                p.getPorcentajeDescuento(),
                p.getImageUrl(), // 👈 USAS TU MÉTODO PRO
                p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                p.getDescription()
        )).toList();
    }

}
