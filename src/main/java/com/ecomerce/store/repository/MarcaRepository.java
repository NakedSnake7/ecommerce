package com.ecomerce.store.repository;

import com.ecomerce.store.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    // Buscar por nombre exacto
    Optional<Marca> findByNombre(String nombre);

    // Validar existencia
    boolean existsByNombre(String nombre);

    // Buscar por nombre ignorando mayúsculas
    Optional<Marca> findByNombreIgnoreCase(String nombre);

    // Listado ordenado
    List<Marca> findAllByOrderByNombreAsc();
}