package com.ecomerce.store.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomerce.store.model.Marca;
import com.ecomerce.store.repository.MarcaRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MarcaService {

    @Autowired
    private MarcaRepository marcaRepository;

    public List<Marca> obtenerTodas() {
        return marcaRepository.findAll()
                .stream()
                .sorted((a,b) ->
                    a.getNombre()
                     .compareToIgnoreCase(b.getNombre()))
                .toList();
    }

    public Marca obtenerPorId(Long id) {

        if(id == null) return null;

        return marcaRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException(
                    "Marca no encontrada ID: " + id
                )
            );
    }

    public Marca obtenerOCrear(String nombre){

        if(nombre == null || nombre.isBlank()){
            return null;
        }

        return marcaRepository
            .findByNombreIgnoreCase(nombre.trim())
            .orElseGet(() -> {

                Marca nueva = new Marca();
                nueva.setNombre(nombre.trim());

                return marcaRepository.save(nueva);
            });
    }

    public void eliminar(Long id){

        if(id != null &&
           marcaRepository.existsById(id)){

            marcaRepository.deleteById(id);
        }
    }
}