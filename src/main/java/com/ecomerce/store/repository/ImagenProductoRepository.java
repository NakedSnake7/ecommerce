package com.ecomerce.store.repository;

import com.ecomerce.store.model.ImagenProducto; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {
	List<ImagenProducto> findByProductoId(Long productoId);
	
    @Modifying
    @Query("DELETE FROM ImagenProducto i WHERE i.producto.id = :productoId")
    void deleteByProductoId(Long productoId);

}
