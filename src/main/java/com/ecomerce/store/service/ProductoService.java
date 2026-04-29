package com.ecomerce.store.service;

import com.ecomerce.store.dto.CloudinaryUploadResult;
import com.ecomerce.store.dto.producto.admin.CategoriaGrupoAdminDTO;
import com.ecomerce.store.dto.producto.admin.ProductoAdminDTO;
import com.ecomerce.store.dto.producto.admin.ProductoAdminListDTO;
import com.ecomerce.store.dto.producto.publico.CategoriaGrupoCardDTO;
import com.ecomerce.store.dto.producto.publico.ProductoCardDTO;
import com.ecomerce.store.dto.producto.publico.ProductoDetailDTO;
import com.ecomerce.store.dto.producto.shared.ProductoVarianteDTO;
import com.ecomerce.store.exception.ImageUploadException;
import com.ecomerce.store.mapper.ProductoAdminListMapper;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final MarcaService marcaService;
    private final ProductoVarianteRepository productoVarianteRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            ImagenProductoRepository imagenProductoRepository,
            CloudinaryService cloudinaryService,
            CategoriaService categoriaService,
            MarcaService marcaService,
            ProductoVarianteRepository productoVarianteRepository
    ) {
        this.productoRepository = productoRepository;
        this.imagenProductoRepository = imagenProductoRepository;
        this.cloudinaryService = cloudinaryService;
        this.categoriaService = categoriaService;
        this.marcaService = marcaService;
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
    public List<ProductoCardDTO> obtenerProductosCompletos() {

        return productoRepository
                .findProductosVisiblesConTodo()
                .stream()
                .map(ProductoMapper::toCard)
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
 // ACTUALIZACIÓN COMPLETA PRO (ESTABLE)
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

     // =====================================================
     // 1. DATOS BASE
     // =====================================================
     producto.setProductName(datos.getProductName());
     producto.setPrice(datos.getPrice());
     producto.setDescription(datos.getDescription());

     producto.setPorcentajeDescuento(
             datos.getPorcentajeDescuento() != null
                     ? datos.getPorcentajeDescuento()
                     : 0.0
     );

     producto.setTienePromocion(
             producto.getPorcentajeDescuento() > 0
     );

     if (datos.getCategoria() != null) {
         producto.setCategoria(datos.getCategoria());
     }

     // permite quitar marca también
     producto.setMarca(datos.getMarca());

     List<String> publicIdsSubidos = new ArrayList<>();

     try {

         // =====================================================
         // 2. ELIMINAR IMÁGENES
         // =====================================================
         if (eliminarImagenes != null && !eliminarImagenes.isEmpty()) {

             producto.getImagenes().removeIf(img -> {

                 boolean eliminar =
                         eliminarImagenes.contains(img.getId());

                 if (eliminar && img.getPublicId() != null) {
                     try {
                         cloudinaryService.eliminarImagen(
                                 img.getPublicId()
                         );
                     } catch (Exception e) {
                         log.warn(
                                 "No se pudo eliminar imagen {}",
                                 img.getPublicId()
                         );
                     }
                 }

                 return eliminar;
             });
         }

         // =====================================================
         // 3. SUBIR NUEVAS IMÁGENES
         // =====================================================
         if (nuevasImagenes != null) {

             for (MultipartFile file : nuevasImagenes) {

                 if (file == null || file.isEmpty()) {
                     continue;
                 }

                 var subida =
                         cloudinaryService.subirImagen(file);

                 ImagenProducto img =
                         new ImagenProducto();

                 img.setProducto(producto);
                 img.setImageUrl(subida.getSecureUrl());
                 img.setPublicId(subida.getPublicId());

                 producto.getImagenes().add(img);

                 publicIdsSubidos.add(
                         subida.getPublicId()
                 );
             }
         }

         // =====================================================
         // 4. VARIANTES
         // SOLO SI EL FORM LAS ENVÍA
         // =====================================================
         if (variantesDTO != null) {

             if (!variantesDTO.isEmpty()) {

                 actualizarVariantes(producto, variantesDTO);
                 producto.setStockSimple(0);

             } else {

                 // producto simple
                 producto.getVariantes().clear();

                 producto.setStockSimple(
                         datos.getStockSimple() != null
                                 ? datos.getStockSimple()
                                 : 0
                 );
             }
         }

         // =====================================================
         // 5. VALIDACIÓN
         // =====================================================
         validarProducto(producto);

         log.info(
                 "Producto actualizado id={}",
                 productoId
         );

         return productoRepository.save(producto);

     } catch (Exception e) {

         // rollback cloudinary
         for (String publicId : publicIdsSubidos) {
             try {
                 cloudinaryService.eliminarImagen(publicId);
             } catch (Exception ignored) {
             }
         }

         log.error(
                 "Error actualizando producto {}",
                 productoId,
                 e
         );

         throw new RuntimeException(
                 "Error actualizando producto",
                 e
         );
     }
 }


 // ============================================================
 // MÉTODO PRO ACTUALIZAR VARIANTES
 // ============================================================

 private void actualizarVariantes(
	        Producto producto,
	        List<ProductoVarianteDTO> variantesDTO
	) {

	    // ============================================
	    // VARIANTES ACTUALES EN BD
	    // ============================================
	    Map<Long, ProductoVariante> actuales =
	            producto.getVariantes()
	                    .stream()
	                    .filter(v -> v.getId() != null)
	                    .collect(Collectors.toMap(
	                            ProductoVariante::getId,
	                            v -> v
	                    ));

	    List<ProductoVariante> nuevasLista =
	            new ArrayList<>();

	    // ============================================
	    // RECORRER DTO
	    // ============================================
	    for (ProductoVarianteDTO dto : variantesDTO) {

	        ProductoVariante variante;

	        // ========================================
	        // EXISTENTE
	        // ========================================
	        if (dto.getId() != null &&
	                actuales.containsKey(dto.getId())) {

	            variante = actuales.get(dto.getId());

	        } else {

	            // ====================================
	            // NUEVA
	            // ====================================
	            variante = new ProductoVariante();
	            variante.setProducto(producto);
	        }

	        // ========================================
	        // PRECIO
	        // ========================================
	        BigDecimal nuevoPrecio =
	                dto.getPrecio() != null
	                        ? dto.getPrecio()
	                        : producto.getPrice();

	        if (!Objects.equals(
	                variante.getPrecio(),
	                nuevoPrecio
	        )) {
	            variante.setPrecio(nuevoPrecio);
	        }

	        // ========================================
	        // STOCK
	        // ========================================
	        Integer nuevoStock =
	                dto.getStock() != null
	                        ? dto.getStock()
	                        : 0;

	        if (!Objects.equals(
	                variante.getStock(),
	                nuevoStock
	        )) {
	            variante.setStock(nuevoStock);
	        }

	        // ========================================
	        // ATRIBUTOS SOLO SI CAMBIARON
	        // ========================================
	        Map<String, String> attrsActuales =
	                variante.getAtributos()
	                        .stream()
	                        .collect(Collectors.toMap(
	                                VarianteAtributo::getNombre,
	                                VarianteAtributo::getValor
	                        ));

	        Map<String, String> attrsNuevos =
	                new LinkedHashMap<>();

	        if (dto.getAtributos() != null) {

	            dto.getAtributos().forEach((k, v) -> {

	                if (k != null &&
	                        !k.isBlank() &&
	                        v != null &&
	                        !v.isBlank()) {

	                    attrsNuevos.put(
	                            k.trim(),
	                            v.trim()
	                    );
	                }
	            });
	        }

	        // SOLO SI CAMBIARON
	        if (!attrsActuales.equals(attrsNuevos)) {

	            variante.getAtributos().clear();

	            attrsNuevos.forEach((k, v) -> {

	                VarianteAtributo attr =
	                        new VarianteAtributo();

	                attr.setNombre(k);
	                attr.setValor(v);
	                attr.setVariante(variante);

	                variante.getAtributos().add(attr);
	            });
	        }

	        nuevasLista.add(variante);
	    }

	    // ============================================
	    // SOLO REEMPLAZAR LISTA SI CAMBIÓ
	    // ============================================
	    boolean cambioCantidad =
	            producto.getVariantes().size()
	                    != nuevasLista.size();

	    boolean cambioIds =
	            !producto.getVariantes()
	                    .stream()
	                    .map(ProductoVariante::getId)
	                    .toList()
	                    .equals(
	                            nuevasLista.stream()
	                                    .map(ProductoVariante::getId)
	                                    .toList()
	                    );

	    if (cambioCantidad || cambioIds) {

	        producto.getVariantes().clear();
	        producto.getVariantes().addAll(nuevasLista);
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
    public ProductoDetailDTO obtenerDetalleProducto(Long id) {

        Producto producto = productoRepository
                .findByIdConTodo(id)
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        return ProductoMapper.toDetail(producto);
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
    public List<CategoriaGrupoCardDTO> obtenerProductosAgrupadosPorCategoria() {

        List<Producto> productos =
                productoRepository.findProductosVisiblesConTodo();

        return productos.stream()
                .collect(Collectors.groupingBy(
                        p -> new AbstractMap.SimpleEntry<>(
                                p.getCategoria().getId(),
                                p.getCategoria().getNombre()
                        ),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                ProductoMapper::toCard,
                                Collectors.toList()
                        )
                ))
                .entrySet()
                .stream()
                .map(e -> new CategoriaGrupoCardDTO(
                        e.getKey().getKey(),
                        e.getKey().getValue(),
                        e.getValue()
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
    		ProductoAdminDTO dto,
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

        // =====================================
        // CATEGORÍA (POR NOMBRE)
        // =====================================
        if(dto.getCategoriaId()!=null){
            producto.setCategoria(
                categoriaService.obtenerPorId(dto.getCategoriaId())
            );
        }else if(dto.getNuevaCategoria()!=null &&
                 !dto.getNuevaCategoria().isBlank()){

            producto.setCategoria(
                categoriaService.obtenerOCrearCategoria(
                    dto.getNuevaCategoria().trim()
                )
            );
        }

        // =====================================
        // MARCA (OPCIONAL)
        // =====================================
        if(dto.getMarcaId() != null){

            producto.setMarca(
                marcaService.obtenerPorId(dto.getMarcaId())
            );

        }else if(dto.getMarcaNombre() != null &&
                 !dto.getMarcaNombre().isBlank()){

            producto.setMarca(
                marcaService.obtenerOCrear(
                    dto.getMarcaNombre().trim()
                )
            );
        }

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
        // VARIANTES
        // =====================================
        if(dto.getVariantes() != null &&
           !dto.getVariantes().isEmpty()){

            producto.setStockSimple(0);

            for(ProductoVarianteDTO vDto :
                dto.getVariantes()){

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

                if(vDto.getAtributos() != null){

                    vDto.getAtributos()
                    .forEach((k,v) -> {

                        if(v != null &&
                           !v.isBlank()){

                            variante.agregarAtributo(
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

            producto.setStockSimple(
                dto.getStockSimple() != null
                ? dto.getStockSimple()
                : 0
            );
        }

        // =====================================
        // GUARDAR
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
    public List<ProductoCardDTO> obtenerProductosIndexOptimizado() {

        return productoRepository.findProductosIndexOptimizado();
    }
    @Transactional(readOnly = true)
    public ProductoAdminDTO obtenerProductoAdmin(Long id) {

        Producto producto = productoRepository
                .findByIdConTodo(id)
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        return ProductoMapper.toAdmin(producto);
    }
    @Transactional(readOnly = true)
    public List<ProductoAdminListDTO> obtenerProductosAdminOptimizado() {

        // 1. carga ligera
        List<Producto> productosBase =
                productoRepository.findProductosAdminBase();

        List<Long> ids = productosBase.stream()
                .map(Producto::getId)
                .toList();

        // 2. carga variantes separadas (sin duplicación SQL)
        List<Producto> productosConVariantes =
                productoRepository.findProductosConVariantes(ids);

        // 3. merge en memoria (MUY importante)
        Map<Long, Producto> map = productosBase.stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        for (Producto p : productosConVariantes) {
            Producto base = map.get(p.getId());
            base.setVariantes(p.getVariantes());
        }

        // 4. mapper final
        return productosBase.stream()
                .map(ProductoAdminListMapper::toDTO)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<CategoriaGrupoAdminDTO> obtenerProductosAdminAgrupados() {

        List<ProductoAdminListDTO> productos =
                obtenerProductosAdminOptimizado();

        return productos.stream()
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
            .map(e -> new CategoriaGrupoAdminDTO(
                e.getKey().getKey(),
                e.getKey().getValue(),
                e.getValue()
            ))
            .toList();
    }
    
  

}
