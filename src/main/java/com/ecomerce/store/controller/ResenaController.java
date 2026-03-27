package com.ecomerce.store.controller;


import com.ecomerce.store.dto.CloudinaryUploadResult; 
import com.ecomerce.store.dto.ResenaEditDTO;
import com.ecomerce.store.entity.ResenaEntity;
import com.ecomerce.store.mapper.ResenaMapper;
import com.ecomerce.store.model.Resena;
import com.ecomerce.store.repository.ResenaRepository;
import com.ecomerce.store.service.CloudinaryService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/resenas")
public class ResenaController {

    private final CloudinaryService cloudinaryService;
    private final ResenaRepository resenaRepository;

    public ResenaController(
            CloudinaryService cloudinaryService,
            ResenaRepository resenaRepository
    ) {
        this.cloudinaryService = cloudinaryService;
        this.resenaRepository = resenaRepository;
    }

    @ModelAttribute("resena")
    public Resena initResena() {
        return new Resena();
    }
    
    // ==========================
    // FORMULARIO ADMIN
    // ==========================
    @GetMapping("/nueva")
    public String formResena(Model model) {

        Resena resena = new Resena();
        resena.setEstrellas(null);

        model.addAttribute("resena", resena);

        model.addAttribute(
            "resenas",
            resenaRepository.findAllByOrderByEstrellasDesc()
                .stream()
                .map(ResenaMapper::toModel)
                .toList()
        );

        return "reviews";
    }

    // ==========================
    // CREAR RESEÑA
    // ==========================
    @PostMapping("/nueva")
    public String guardarResena(@ModelAttribute Resena resena, Model model) {

        try {
            if (resena.getImagen() == null || resena.getImagen().isEmpty()) {
                throw new IllegalArgumentException("La imagen es obligatoria");
            }

            CloudinaryUploadResult upload =
                    cloudinaryService.subirImagen(resena.getImagen());

            resena.setImagenUrl(upload.getSecureUrl());
            resena.setPublicId(upload.getPublicId());

            ResenaEntity entity = ResenaMapper.toEntity(resena);
            resenaRepository.save(entity);

            return "redirect:/resenas/nueva?success";

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("resena", resena);
            model.addAttribute(
            	    "resenas",
            	    resenaRepository.findAllByOrderByEstrellasDesc()
            	        .stream()
            	        .map(ResenaMapper::toModel)
            	        .toList()
            	);

            return "reviews";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/resenas/nueva?error";
        }
    }

    // ==========================
    // ELIMINAR RESEÑA
    // ==========================
    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminarResena(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();

        return resenaRepository.findById(id).map(resena -> {

            if (resena.getPublicId() != null) {
                cloudinaryService.eliminarImagen(resena.getPublicId());
            }

            resenaRepository.delete(resena);

            response.put("success", true);
            return response;

        }).orElseGet(() -> {
            response.put("success", false);
            return response;
        });
    }

    // ==========================
    // EDITAR
    // ==========================
    @PutMapping("/{id}")
    @ResponseBody
    public ResenaEntity editarResena(
            @PathVariable Long id,
            @RequestBody ResenaEditDTO dto) {

        ResenaEntity resena = resenaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrada"));

        resena.setComentario(dto.getComentario());
        resena.setEstrellas(dto.getEstrellas());

        return resenaRepository.save(resena);
    }
    
    @GetMapping
    public String listarResenas(Model model) {

        model.addAttribute("resena", new Resena()); // 👈 FALTABA ESTO

        model.addAttribute(
            "resenas",
            resenaRepository.findAllByOrderByEstrellasDesc()
                .stream()
                .map(ResenaMapper::toModel)
                .toList()
        );

        return "reviews";
    }

    // ==========================
    // ACTUALIZAR IMAGEN
    // ==========================
    @PutMapping("/{id}/imagen")
    @ResponseBody
    public ResenaEntity actualizarImagen(
            @PathVariable Long id,
            @RequestParam("imagen") MultipartFile imagen) {

        ResenaEntity resena = resenaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrada"));

        try {
            if (resena.getPublicId() != null) {
                cloudinaryService.eliminarImagen(resena.getPublicId());
            }

            CloudinaryUploadResult upload =
                    cloudinaryService.subirImagen(imagen);

            resena.setImagenUrl(upload.getSecureUrl());
            resena.setPublicId(upload.getPublicId());

            return resenaRepository.save(resena);

        } catch (Exception e) {
            throw new RuntimeException("Error al subir imagen");
        }
    }

    // ==========================
    // ELIMINAR IMAGEN
    // ==========================
    @DeleteMapping("/{id}/imagen")
    @ResponseBody
    public ResponseEntity<?> eliminarImagen(@PathVariable Long id) {

        ResenaEntity resena = resenaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrada"));

        if (resena.getPublicId() != null) {
            cloudinaryService.eliminarImagen(resena.getPublicId());
        }

        resena.setImagenUrl(null);
        resena.setPublicId(null);

        resenaRepository.save(resena);

        return ResponseEntity.ok().build();
    }
}