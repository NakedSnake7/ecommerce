package com.webempresarial.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.entity.ResenaEntity;

import java.util.List;

public interface ResenaRepository extends JpaRepository<ResenaEntity, Long> {

    // 🔥 Ordenar por estrellas DESC
    List<ResenaEntity> findAllByOrderByEstrellasDesc();
}
