package com.ecomerce.store.controller;

import com.ecomerce.store.dto.ProductoDTO;
import com.ecomerce.store.dto.ProductoVarianteDTO;
import com.ecomerce.store.model.Producto;

import com.ecomerce.store.service.ProductoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecomerce.store.service.CategoriaService;

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


    // ==================================================
    // LISTA DE PRODUCTOS
    // ==================================================
    @GetMapping("/VerProductos")
    public String verProductos(Model model) {
    	

        model.addAttribute(
            "categorias",
            productoService.obtenerProductosAgrupadosPorCategoria()
        );

        return "VerProductos";
    }



    // ==================================================
    // FORMULARIO NUEVO
    // ==================================================
    @GetMapping("/nuevo")
    public String formularioNuevoProducto(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        return "subirProducto";
    }

    // ==================================================
    // GUARDAR NUEVO
    // ==================================================
 // CONTROLLER PRO JSON STRIDE

    @PostMapping("/nuevo")
    public String guardarProducto(

            @Valid @ModelAttribute("producto") ProductoDTO dto,
            BindingResult result,

            @RequestParam(value="imagenes",required=false)
            List<MultipartFile> imagenes,

            @RequestParam(value="variantesJson",required=false)
            String variantesJson,

            Model model) {

        if(result.hasErrors()){

            model.addAttribute(
                "categorias",
                categoriaService.obtenerTodas()
            );

            return "subirProducto";
        }

        try{

            // convertir JSON -> lista variantes
            if(variantesJson != null &&
               !variantesJson.isBlank()){

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

            productoService.crearProducto(dto,imagenes);

            return "redirect:/VerProductos";

        }catch(Exception e){

            e.printStackTrace();

            result.reject(
                "error.producto",
                "Error al guardar producto"
            );

            model.addAttribute(
                "categorias",
                categoriaService.obtenerTodas()
            );

            return "subirProducto";
        }
    }
    
    // ==================================================
    // FORMULARIO EDITAR
    // ==================================================
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {

        //  Obtener DTO ya mapeado correctamente
        ProductoDTO dto = productoService.obtenerProductoDTO(id);

        //  Cargar categorías para el select
        model.addAttribute(
            "categorias",
            categoriaService.obtenerTodas()
        );

        //  Enviar producto al formulario
        model.addAttribute(
            "productoDTO",
            dto
        );

        return "EditarProducto";
    }

    // ==================================================
    // GUARDAR EDICIÓN
    // ==================================================
    @PostMapping("/editar/{id}")
    public String editarProducto(
            @PathVariable Long id,
            @Valid @ModelAttribute("productoDTO") ProductoDTO dto,
            BindingResult result,
            @RequestParam(value = "imagenes", required = false)
            List<MultipartFile> nuevasImagenes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categorias",
                categoriaService.obtenerTodas());

            return "EditarProducto";
        }

        try {

            Producto datos = new Producto();

            datos.setProductName(dto.getProductName());
            datos.setPrice(dto.getPrice());
            datos.setDescription(dto.getDescription());
            datos.setPorcentajeDescuento(
                dto.getPorcentajeDescuento()
            );

            // 🔥 FIX STOCK SIMPLE
            datos.setStockSimple(
                dto.getStockSimple()
            );

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

            productoService.actualizarProductoCompleto(
                id,
                datos,
                nuevasImagenes,
                dto.getImagenesEliminar(),
                dto.getVariantes()
            );

            return "redirect:/VerProductos";

        } catch (Exception e) {

            result.reject(
                "error.producto",
                "Error al actualizar producto"
            );

            model.addAttribute(
                "categorias",
                categoriaService.obtenerTodas()
            );

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

        model.addAttribute("productos", productoService.obtenerProductosCompletos()); // DTO
        model.addAttribute("categorias", productoService.obtenerCategorias());

        return "modificar-precios";
    }

}
