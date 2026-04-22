package com.ecomerce.store.service;

import com.ecomerce.store.dto.CategoriaGrupoDTO;      

import com.ecomerce.store.dto.CloudinaryUploadResult;
import com.ecomerce.store.dto.ProductoDTO;
import com.ecomerce.store.dto.ProductoPrecioDTO;
import com.ecomerce.store.dto.ProductoResumenDTO;
import com.ecomerce.store.dto.ProductoVarianteDTO;
import com.ecomerce.store.exception.ImageUploadException;
import com.ecomerce.store.mapper.ProductoMapper;
import com.ecomerce.store.model.ImagenProducto;
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.model.ProductoVariante;
import com.ecomerce.store.model.VarianteAtributo;
import com.ecomerce.store.repository.ImagenProductoRepository;
import com.ecomerce.store.repository.ProductoRepository;
import com.ecomerce.store.repository.ProductoVarianteRepository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductoService {

    private static final Logger log =
            LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final ImagenProductoRepository imagenProductoRepository;
    private final CloudinaryService cloudinaryService;
    private final CategoriaService categoriaService;
    private final ProductoVarianteRepository productoVarianteRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            ImagenProductoRepository imagenProductoRepository,
            CloudinaryService cloudinaryService,
            CategoriaService categoriaService,
            ProductoVarianteRepository productoVarianteRepository
    ) {
        this.productoRepository = productoRepository;
        this.imagenProductoRepository = imagenProductoRepository;
        this.cloudinaryService = cloudinaryService;
        this.categoriaService = categoriaService;
        this.productoVarianteRepository = productoVarianteRepository;
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

        return productoRepository
            .findProductosVisiblesConTodo()
            .stream()
            .map(producto -> {

                ProductoDTO dto = ProductoMapper.toDTO(producto);

                // ===============================
                // VARIANTES CON ATRIBUTOS REALES
                // ===============================
                if(producto.getVariantes() != null &&
                   !producto.getVariantes().isEmpty()){

                    List<ProductoVarianteDTO> variantes =
                        producto.getVariantes()
                        .stream()
                        .map(v -> {

                            ProductoVarianteDTO vd =
                                new ProductoVarianteDTO();

                            vd.setId(v.getId());
                            vd.setNombre(v.getNombreVisual());
                            vd.setStock(v.getStock());
                            vd.setPrecio(v.getPrecio());

                            // 🔥 CLAVE DEL PROBLEMA
                            vd.setAtributos(
                                v.getAtributosMap()
                            );

                            return vd;
                        })
                        .toList();

                    dto.setVariantes(variantes);

                    // ===============================
                    // PRECIO MINIMO
                    // ===============================
                    dto.setPrecioMinimo(
                        variantes.stream()
                        .map(x ->
                            x.getPrecio() != null
                            ? x.getPrecio()
                            : dto.getPrecio()
                        )
                        .min(BigDecimal::compareTo)
                        .orElse(dto.getPrecio())
                    );
                }

                // ===============================
                // PRECIO FINAL PROMO
                // ===============================
                dto.setPrecioFinal(
                    dto.getPrecioConDescuento()
                );

                return dto;
            })
            .toList();
    }
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerProductosParaPrecios() {

        return productoRepository.findProductosVisiblesConTodo()
                .stream()
                .map(ProductoMapper::toDTO)
                .toList();
    }
    public List<ProductoPrecioDTO> obtenerProductosPrecio() {

        return productoRepository.findProductosPrecioRaw()
                .stream()
                .map(r -> new ProductoPrecioDTO(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        (BigDecimal) r[2],
                        (BigDecimal) r[3],
                        (Boolean) r[4],
                        (Boolean) r[5],
                        (String) r[6]
                ))
                .toList();
    }
    
    @Transactional
    public Producto guardarProducto(Producto producto) {
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No existe"));

        producto.getImagenes().forEach(img -> {
            try {
                cloudinaryService.eliminarImagen(img.getPublicId());
            } catch (Exception e) {
                log.warn("Error eliminando imagen {}", img.getPublicId());
            }
        });

        productoRepository.deleteById(id);
        productoRepository.flush();
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
            List<Long> eliminarImagenes,
            List<ProductoVarianteDTO> variantesDTO
    ) {

        Producto producto = obtenerProducto(productoId);

        // =========================
        // 1. DATOS BASE
        // =========================
        producto.setProductName(datos.getProductName());
        producto.setPrice(datos.getPrice());
        producto.setDescription(datos.getDescription());
        producto.setPorcentajeDescuento(datos.getPorcentajeDescuento());
        producto.setCategoria(datos.getCategoria());

        validarProducto(producto);

        List<String> publicIdsSubidos = new ArrayList<>();

        try {

            // =========================
            // 2. ELIMINAR IMÁGENES
            // =========================
            if (eliminarImagenes != null && !eliminarImagenes.isEmpty()) {

                producto.getImagenes().removeIf(img -> {

                    boolean eliminar = eliminarImagenes.contains(img.getId());

                    if (eliminar && img.getPublicId() != null) {
                        try {
                            cloudinaryService.eliminarImagen(img.getPublicId());
                        } catch (Exception e) {
                            log.warn("Error eliminando imagen Cloudinary {}", img.getPublicId());
                        }
                    }

                    return eliminar;
                });
            }

            // =========================
            // 3. SUBIR NUEVAS IMÁGENES
            // =========================
            if (nuevasImagenes != null) {

                for (MultipartFile file : nuevasImagenes) {

                    // 🔥 IGNORAR ARCHIVOS VACÍOS
                    if (file == null || file.isEmpty()) {
                        continue;
                    }

                    try {
                        var resultado = cloudinaryService.subirImagen(file);

                        ImagenProducto img = new ImagenProducto();
                        img.setImageUrl(resultado.getSecureUrl());
                        img.setPublicId(resultado.getPublicId());
                        img.setProducto(producto);

                        producto.getImagenes().add(img);
                        imagenProductoRepository.save(img);
                        publicIdsSubidos.add(resultado.getPublicId());

                    } catch (IOException e) {
                        log.error("Error subiendo imagen", e);
                        throw new RuntimeException("Error subiendo imagen", e);
                    }
                }
            }

         // =========================
         // 4. SINCRONIZAR VARIANTES
         // =========================
         if (variantesDTO != null && !variantesDTO.isEmpty()) {

             Map<Long, ProductoVariante> existentes = producto.getVariantes()
                     .stream()
                     .collect(Collectors.toMap(
                             ProductoVariante::getId,
                             v -> v
                     ));

             List<ProductoVariante> nuevasLista = new ArrayList<>();

             for (ProductoVarianteDTO dto : variantesDTO) {

                 ProductoVariante variante;

                 // EXISTENTE
                 if (dto.getId() != null &&
                     existentes.containsKey(dto.getId())) {

                     variante = existentes.get(dto.getId());

                 } else {

                     // NUEVA
                     variante = new ProductoVariante();
                     variante.setProducto(producto);
                 }

                 variante.setPrecio(dto.getPrecio());

                 variante.setStock(
                     dto.getStock() != null
                         ? dto.getStock()
                         : 0
                 );

                 // =========================
                 // ATRIBUTOS
                 // =========================
                 if (variante.getAtributos() == null) {
                     variante.setAtributos(
                         new LinkedHashSet<>()
                     );
                 } else {
                     variante.getAtributos().clear();
                 }

                 if (dto.getAtributos() != null) {

                     dto.getAtributos()
                     .forEach((key, value) -> {

                         VarianteAtributo attr =
                             new VarianteAtributo();

                         attr.setNombre(key);
                         attr.setValor(value);
                         attr.setVariante(variante);

                         variante.getAtributos()
                                 .add(attr);
                     });
                 }

                 nuevasLista.add(variante);
             }

             // reemplazo completo
             producto.getVariantes().clear();
             producto.getVariantes().addAll(nuevasLista);

             // 🔥 Si tiene variantes,
             // stock simple se desactiva
             producto.setStockSimple(0);

         } else {

             // 🔥 Producto simple sin variantes
             producto.getVariantes().clear();

             producto.setStockSimple(
                 datos.getStockSimple() != null
                     ? datos.getStockSimple()
                     : 0
             );
         }

            // =========================
            // 5. GUARDAR
            // =========================
            log.info("Actualizando producto id={}", productoId);

            productoRepository.save(producto);

            return producto;

        } catch (Exception e) {

            // =========================
            // ROLLBACK CLOUDINARY
            // =========================
            for (String publicId : publicIdsSubidos) {
                try {
                    cloudinaryService.eliminarImagen(publicId);
                } catch (Exception ignored) {}
            }

            log.error("Error actualizando producto {}", productoId, e);

            throw e;
        }
    }
    
    @Transactional
    public void actualizarStockVariante(Long varianteId, Integer stock) {

        ProductoVariante variante = productoVarianteRepository.findById(varianteId)
            .orElseThrow(() -> new RuntimeException("Variante no existe"));

        variante.setStock(stock);
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
    
    @Transactional(readOnly = true)
    public ProductoDTO obtenerProductoDTO(Long id) {
        Producto producto = productoRepository.findByIdConTodo(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));

        return ProductoMapper.toDTO(producto);
    } 
    
    @Transactional
    public void subirImagenesProducto(
            Long productoId,
            List<MultipartFile> imagenes
    ) {
        Producto producto = obtenerProducto(productoId);

        List<String> publicIds = new ArrayList<>();
        subirImagenesInterno(producto, imagenes, publicIds);
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
        
        if (producto.getVariantes() == null || producto.getVariantes().isEmpty()) {
            log.warn("Producto sin variantes id={}", producto.getId());
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
            .map(ProductoMapper::toDTO)
            .collect(Collectors.groupingBy(
                p -> new AbstractMap.SimpleEntry<>(
                    p.getCategoriaId(),
                    p.getCategoriaNombre()
                ),
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet()
            .stream()
            .map(e -> new CategoriaGrupoDTO(
                e.getKey().getKey(),     //  ID categoría
                e.getKey().getValue(),   //  Nombre categoría
                e.getValue()             //  Productos
            ))
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

        Producto producto = productoRepository.findByIdConTodo(id) //  FIX
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getVariantes() != null && !producto.getVariantes().isEmpty()) {
            throw new IllegalStateException("Producto con variantes no usa precio base");
        }

        producto.setPrice(precio.setScale(2, RoundingMode.HALF_UP));
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
    
    @Transactional
    public Producto crearProducto(
            ProductoDTO dto,
            List<MultipartFile> imagenes) {

        Producto producto = new Producto();

        // =====================================
        // DATOS BASE
        // =====================================
        producto.setProductName(
            dto.getProductName().trim()
        );

        producto.setPrice(
            dto.getPrecio()
        );

        producto.setDescription(
            dto.getDescription()
        );

        producto.setCategoria(
            categoriaService.obtenerPorId(
                dto.getCategoriaId()
            )
        );

        producto.setVisibleEnMenu(true);

        // =====================================
        // PROMOCIÓN
        // =====================================
        Double descuento =
            dto.getPorcentajeDescuento() != null
            ? dto.getPorcentajeDescuento()
            : 0.0;

        producto.setPorcentajeDescuento(
            descuento
        );

        producto.setTienePromocion(
            descuento > 0
        );

        // =====================================
        // VARIANTES JSON PRO
        // =====================================
        if(dto.getVariantes() != null &&
           !dto.getVariantes().isEmpty()){

            producto.setStockSimple(0);

            for(ProductoVarianteDTO vDto :
                dto.getVariantes()){

                // ignorar basura vacía
                if(vDto.getStock() == null &&
                   vDto.getPrecio() == null){
                    continue;
                }

                ProductoVariante variante =
                    new ProductoVariante();

                variante.setProducto(producto);

                variante.setStock(
                    vDto.getStock() != null
                    ? vDto.getStock()
                    : 0
                );

                variante.setPrecio(
                    vDto.getPrecio() != null
                    ? vDto.getPrecio()
                    : dto.getPrecio()
                );

              

                // atributos dinámicos
                if(vDto.getAtributos()!=null){

                    vDto.getAtributos()
                    .forEach((k,v)->{

                        if(v != null &&
                           !v.isBlank()){

                            variante
                            .agregarAtributo(
                                k.trim(),
                                v.trim()
                            );
                        }

                    });
                }

                producto.agregarVariante(
                    variante
                );
            }

        }else{

            // simple product
            producto.setStockSimple(
                dto.getStockSimple() != null
                ? dto.getStockSimple()
                : 0
            );
        }

        // =====================================
        // SAVE
        // =====================================
        Producto saved =
            productoRepository.save(producto);

        // =====================================
        // IMÁGENES
        // =====================================
        if(imagenes != null &&
           !imagenes.isEmpty()){

            subirImagenesProducto(
                saved.getId(),
                imagenes
            );
        }

        return saved;
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
