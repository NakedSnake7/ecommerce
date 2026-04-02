package com.ecomerce.store.repository;

import com.ecomerce.store.model.ProductoVariante;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductoVariante v WHERE v.id = :id")
    Optional<ProductoVariante> findByIdForUpdate(@Param("id") Long id);
    
   
}