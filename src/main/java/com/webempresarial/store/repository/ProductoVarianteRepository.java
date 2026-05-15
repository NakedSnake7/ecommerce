package com.webempresarial.store.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.webempresarial.store.model.ProductoVariante;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductoVariante v WHERE v.id = :id")
    Optional<ProductoVariante> findByIdForUpdate(@Param("id") Long id);
    
    @Modifying
    @Query("DELETE FROM ProductoVariante v WHERE v.producto.id = :productoId")
    void deleteByProductoId(Long productoId);
    
   
}