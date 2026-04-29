package com.ecomerce.store.controller;

import com.ecomerce.store.dto.producto.admin.ProductoAdminDTO;
import com.ecomerce.store.dto.producto.shared.ProductoVarianteDTO;
import com.ecomerce.store.model.Producto;

import com.ecomerce.store.service.ProductoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecomerce.store.service.CategoriaService;
import com.ecomerce.store.service.MarcaService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/")
public class ProductoViewController {

    @Autowired private ProductoService productoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired
    private MarcaService marcaService;

    // ==================================================
    // LISTA DE PRODUCTOS
    // ==================================================
    @GetMapping("/admin/productos")
    public String verProductos(Model model) {

        model.addAttribute(
            "categorias",
            productoService.obtenerProductosAdminAgrupados()
        );

        return "VerProductos";
    }



    // ==================================================
    // FORMULARIO NUEVO
    // ==================================================
    @GetMapping("/nuevo")
    public String formularioNuevoProducto(Model model) {

        model.addAttribute("producto", new ProductoAdminDTO());

        model.addAttribute(
            "categorias",
            categoriaService.obtenerTodas()
        );

        model.addAttribute(
            "marcas",
            marcaService.obtenerTodas()
        );

        return "subirProducto";
    }

    // ==================================================
    // GUARDAR NUEVO
    // ==================================================

    @PostMapping("/nuevo")
    public String guardarProducto(

            @Valid @ModelAttribute("producto") ProductoAdminDTO dto,
            BindingResult result,

            @RequestParam(value = "imagenes", required = false)
            List<MultipartFile> imagenes,

            @RequestParam(value = "variantesJson", required = false)
            String variantesJson,

            @RequestParam(value = "nuevaCategoria", required = false)
            String nuevaCategoria,

            @RequestParam(value = "nuevaMarca", required = false)
            String nuevaMarca,

            Model model) {

        // ==================================================
        // VALIDACIONES DEL FORM
        // ==================================================
        if (result.hasErrors()) {

            model.addAttribute(
                    "categorias",
                    categoriaService.obtenerTodas()
            );

            model.addAttribute(
                    "marcas",
                    marcaService.obtenerTodas()
            );

            return "subirProducto";
        }

        try {

            // ==================================================
            // CATEGORÍA
            // ==================================================

            // Si escribió una nueva categoría manual
            if (nuevaCategoria != null &&
                !nuevaCategoria.isBlank()) {

                dto.setCategoriaId(

                    categoriaService
                        .obtenerOCrearCategoria(
                            nuevaCategoria.trim()
                        )
                        .getId()
                );

            } else if (dto.getCategoriaId() != null) {

                // Si seleccionó una categoría existente
                // simplemente usamos esa ID.
                // Si no existe en BD la crea automática
                dto.setCategoriaId(

                    categoriaService
                        .obtenerPorId(dto.getCategoriaId())
                        .getId()
                );
            }

            // ==================================================
            // MARCA
            // ==================================================

            if (nuevaMarca != null && !nuevaMarca.isBlank()) {
                dto.setMarcaId(
                    marcaService
                        .obtenerOCrear(nuevaMarca)
                        .getId()
                );
            }

            // ==================================================
            // CONVERTIR JSON VARIANTES
            // ==================================================

            if (variantesJson != null &&
                !variantesJson.isBlank()) {

                ObjectMapper mapper =
                        new ObjectMapper();

                List<ProductoVarianteDTO> variantes =
                        mapper.readValue(
                                variantesJson,
                                new TypeReference<
                                    List<ProductoVarianteDTO>>() {}
                        );

                dto.setVariantes(variantes);
            }

            // ==================================================
            // GUARDAR PRODUCTO
            // ==================================================

            productoService.crearProducto(
                    dto,
                    imagenes
            );

            return "redirect:/VerProductos";

        } catch (Exception e) {

            e.printStackTrace();

            result.reject(
                    "error.producto",
                    "Error al guardar producto: "
                            + e.getMessage()
            );

            model.addAttribute(
                    "categorias",
                    categoriaService.obtenerTodas()
            );

            model.addAttribute(
                    "marcas",
                    marcaService.obtenerTodas()
            );

            model.addAttribute(
                    "producto",
                    dto
            );

            return "subirProducto";
        }
    }
    
    // ==================================================
    // FORMULARIO EDITAR
    // ==================================================
    @GetMapping("/editar/{id}")
    public String formularioEditar(
            @PathVariable Long id,
            Model model) {

    	ProductoAdminDTO dto = productoService.obtenerProductoAdmin(id);

        model.addAttribute(
            "productoDTO",
            dto
        );

        model.addAttribute(
            "categorias",
            categoriaService.obtenerTodas()
        );

        model.addAttribute(
            "marcas",
            marcaService.obtenerTodas()
        );

        return "EditarProducto";
    }

    // ==================================================
    // GUARDAR EDICIÓN
    // ==================================================
    @PostMapping("/editar/{id}")
    public String editarProducto(
            @PathVariable Long id,
            @Valid @ModelAttribute("productoDTO") ProductoAdminDTO dto,
            BindingResult result,
            @RequestParam(value="imagenes", required=false)
            List<MultipartFile> nuevasImagenes,
            Model model) {

        if (result.hasErrors()) {

            model.addAttribute("productoDTO", dto);
            model.addAttribute("categorias", categoriaService.obtenerTodas());
            model.addAttribute("marcas", marcaService.obtenerTodas());

            return "EditarProducto";
        }

        try {

            Producto datos = new Producto();

            datos.setProductName(dto.getProductName());
            datos.setPrice(dto.getPrecio());
            datos.setDescription(dto.getDescription());
            datos.setPorcentajeDescuento(dto.getPorcentajeDescuento());
            datos.setStockSimple(dto.getStockSimple());

            if (dto.getNuevaCategoria() != null &&
                !dto.getNuevaCategoria().isBlank()) {

                datos.setCategoria(
                    categoriaService.obtenerOCrearCategoria(
                        dto.getNuevaCategoria()
                    )
                );

            } else {

                datos.setCategoria(
                    categoriaService.obtenerPorId(
                        dto.getCategoriaId()
                    )
                );
            }

            if (dto.getMarcaNombre() != null && !dto.getMarcaNombre().isBlank()) {
                datos.setMarca(
                    marcaService.obtenerOCrear(dto.getMarcaNombre())
                );
            } else if (dto.getMarcaId() != null) {
                datos.setMarca(
                    marcaService.obtenerPorId(dto.getMarcaId())
                );
            }

            productoService.actualizarProductoCompleto(
                id,
                datos,
                nuevasImagenes,
                dto.getImagenesEliminar(),
                dto.getVariantes()
            );

            return "redirect:/VerProductos";

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute("productoDTO", dto);
            model.addAttribute("categorias", categoriaService.obtenerTodas());
            model.addAttribute("marcas", marcaService.obtenerTodas());

            return "EditarProducto";
        }
    }
    @PostMapping("/variantes/{id}/stock")
    public String actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {

        productoService.actualizarStockVariante(id, stock);

        return "redirect:/VerProductos";
    }

    @GetMapping("/modificar-precios")
    public String vistaModificarPrecios(Model model) {

        model.addAttribute(
            "productos",
            productoService.obtenerProductosAdminOptimizado()
        );

        model.addAttribute(
            "categorias",
            productoService.obtenerCategorias()
        );

        return "modificar-precios";
    }

}
