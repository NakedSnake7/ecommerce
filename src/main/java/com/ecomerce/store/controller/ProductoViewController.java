package com.ecomerce.store.controller;

import com.ecomerce.store.dto.ProductoDTO;   
import com.ecomerce.store.model.Producto;
import com.ecomerce.store.service.ProductoService;
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
    @PostMapping("/nuevo")
    public String guardarProducto(
            @Valid @ModelAttribute("producto") ProductoDTO dto,
            BindingResult result,
            @RequestParam(value = "imagenes", required = false) List<MultipartFile> imagenes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.obtenerTodas());
            return "subirProducto";
        }

        Producto producto = new Producto();
        producto.setProductName(dto.getProductName());
        producto.setPrice(dto.getPrice());
        producto.setDescription(dto.getDescription());
        producto.setStock(dto.getStock());
        producto.setCategoria(
                categoriaService.obtenerOCrearCategoria(dto.getCategoriaNombre())
        );

        productoService.actualizarProductoCompleto(
                productoService.guardarProducto(producto).getId(),
                producto,
                imagenes,
                null
        );

        return "redirect:/VerProductos";
    }

    // ==================================================
    // FORMULARIO EDITAR
    // ==================================================
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {

        Producto producto = productoService.obtenerProducto(id);

        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setProductName(producto.getProductName());
        dto.setPrice(producto.getPrice());
        dto.setDescription(producto.getDescription());
        dto.setStock(producto.getStock());
        dto.setCategoriaNombre(producto.getCategoria().getNombre());
        dto.setPorcentajeDescuento(producto.getPorcentajeDescuento());

        producto.getImagenes().forEach(img -> {
            dto.getImagenesExistentes().add(img.getId());
            dto.getUrlsImagenesExistentes().add(img.getImageUrl());
        });

        model.addAttribute("productoDTO", dto);
        model.addAttribute("categorias", categoriaService.obtenerTodas());
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
            @RequestParam(value = "imagenes", required = false) List<MultipartFile> nuevasImagenes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.obtenerTodas());
            return "EditarProducto";
        }

        Producto datos = new Producto();
        datos.setProductName(dto.getProductName());
        datos.setPrice(dto.getPrice());
        datos.setDescription(dto.getDescription());
        datos.setStock(dto.getStock());
        datos.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        datos.setCategoria(
                categoriaService.obtenerOCrearCategoria(dto.getCategoriaNombre())
        );

        productoService.actualizarProductoCompleto(
                id,
                datos,
                nuevasImagenes,
                dto.getImagenesExistentes()
        );

        return "redirect:/VerProductos";
    }


    @GetMapping("/modificar-precios")
    public String vistaModificarPrecios(Model model) {

        model.addAttribute("productos", productoService.obtenerTodosLosProductos());
        model.addAttribute("categorias", productoService.obtenerCategorias());


        return "modificar-precios";
    }

}
