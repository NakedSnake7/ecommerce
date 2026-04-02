package com.ecomerce.store.service;

import java.util.List; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomerce.store.model.Categoria;
import com.ecomerce.store.repository.CategoriaRepository;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> obtenerTodas() {
        List<Categoria> categorias = categoriaRepository.findAll();
        System.out.println("🔎 Categorías obtenidas: " + categorias);
        return categorias; // ✅ devuelve la misma lista que imprimes
    }


    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
        } else {
            throw new RuntimeException("No se puede eliminar, categoría no encontrada con ID: " + id);
        }
    }
  
    public Categoria obtenerOCrearCategoria(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("La categoría no puede ser nula o vacía");
        }

        return categoriaRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseGet(() -> {
                    Categoria nueva = new Categoria();
                    nueva.setNombre(nombre.trim());
                    System.out.println("🆕 Creando nueva categoría: " + nombre);
                    return categoriaRepository.save(nueva);
                });
    }


}
