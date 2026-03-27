package com.ecomerce.store.repository;

import com.ecomerce.store.entity.ResenaEntity;  
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<ResenaEntity, Long> {

    // 🔥 Ordenar por estrellas DESC
    List<ResenaEntity> findAllByOrderByEstrellasDesc();
}
