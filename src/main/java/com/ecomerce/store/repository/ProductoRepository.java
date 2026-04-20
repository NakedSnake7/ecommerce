package com.ecomerce.store.repository;

import com.ecomerce.store.dto.ProductoResumenDTO;
import com.ecomerce.store.model.Producto; 

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

  

    Optional<Producto> findByProductName(String productName);
    
    @EntityGraph(attributePaths = {"categoria"})
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdWithCategoria(@Param("id") Long id);

    @Query("SELECT DISTINCT p.categoria.nombre FROM Producto p WHERE p.visibleEnMenu = true")
    List<String> obtenerNombresCategoriasVisibles();

    
    @Query("""
    	    SELECT DISTINCT p FROM Producto p
    	    LEFT JOIN FETCH p.categoria
    	    LEFT JOIN FETCH p.variantes
    	    LEFT JOIN FETCH p.imagenes
    	    WHERE p.visibleEnMenu = true
    	""")
    	List<Producto> findProductosVisiblesConTodo();
    
    @Query("""
    		SELECT new com.ecomerce.store.dto.ProductoResumenDTO(
    		    p.id,
    		    p.productName,
    		    p.price,
    		    p.tienePromocion,
    		    p.porcentajeDescuento,
    		    null,
    		    c.nombre,
    		    p.description
    		)
    		FROM Producto p
    		LEFT JOIN p.categoria c
    		WHERE p.visibleEnMenu = true
    		""")
    		List<ProductoResumenDTO> findProductosIndex();
    
    @Query("""
    		SELECT DISTINCT p FROM Producto p
    		LEFT JOIN FETCH p.categoria
    		LEFT JOIN FETCH p.imagenes
    		LEFT JOIN FETCH p.variantes v
    		LEFT JOIN FETCH v.atributos
    		WHERE p.id = :id
    		""")
    		Optional<Producto> findByIdConTodo(@Param("id") Long id);
    
 
    
    @Query("SELECT DISTINCT p FROM Producto p " +
    	       "LEFT JOIN FETCH p.imagenes " +
    	       "LEFT JOIN FETCH p.categoria " +
    	       "WHERE p.productName = :name")
    	Optional<Producto> findByProductNameConTodo(@Param("name") String name);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);
    
    @Query("SELECT p FROM Producto p WHERE p.categoria.nombre = :categoria")
    List<Producto> findByCategoriaNombre(@Param("categoria") String categoria);
    
    List<Producto> findByCategoriaId(Long categoriaId);

    @Modifying
    @Query("UPDATE Producto p SET p.visibleEnMenu = :visible WHERE p.categoria.id = :categoriaId")
    void updateVisibilidadPorCategoria(@Param("categoriaId") Long categoriaId,
                                       @Param("visible") boolean visible);
    
    @Query("""
    		SELECT DISTINCT p FROM Producto p
    		LEFT JOIN FETCH p.variantes v
    		LEFT JOIN FETCH v.atributos
    		WHERE p.id = :id
    		""")
    		Producto findByIdWithVariantes(@Param("id") Long id);
    
    @Query(value = """
    		SELECT 
    		    p.id,
    		    p.product_name,
    		    p.price,

    		    -- PRECIO MINIMO (si hay variantes)
    		    COALESCE(
    		        (SELECT MIN(v.precio) 
    		         FROM producto_variantes v 
    		         WHERE v.producto_id = p.id),
    		        p.price
    		    ) AS precio_minimo,

    		    -- TIENE VARIANTES
    		    CASE 
    		        WHEN EXISTS (
    		            SELECT 1 
    		            FROM producto_variantes v 
    		            WHERE v.producto_id = p.id
    		        ) THEN true
    		        ELSE false
    		    END AS tiene_variantes,

    		    p.tiene_promocion,

    		    c.nombre AS categoria_nombre

    		FROM productos p
    		LEFT JOIN categorias c ON c.id = p.categoria_id

    		WHERE p.visible_en_menu = 1
    		""", nativeQuery = true)
    		List<Object[]> findProductosPrecioRaw();

}
