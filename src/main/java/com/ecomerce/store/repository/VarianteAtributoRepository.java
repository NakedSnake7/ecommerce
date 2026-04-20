package com.ecomerce.store.repository;

import com.ecomerce.store.model.VarianteAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VarianteAtributoRepository extends JpaRepository<VarianteAtributo, Long> {

    @Modifying
    @Query("""
        DELETE FROM VarianteAtributo a
        WHERE a.variante.id IN (
            SELECT v.id FROM ProductoVariante v WHERE v.producto.id = :productoId
        )
    """)
    void deleteByProductoId(Long productoId);
}