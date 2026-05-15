package com.webempresarial.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.model.Categoria;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
