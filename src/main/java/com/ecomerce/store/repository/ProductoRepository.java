package com.ecomerce.store.repository;

import com.ecomerce.store.dto.producto.publico.ProductoCardDTO; 
import com.ecomerce.store.model.Producto;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // =====================================
    // BASIC
    // =====================================

    Optional<Producto> findByProductName(String productName);

    List<Producto> findByCategoriaId(Long categoriaId);

    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.categoria.nombre = :categoria
    """)
    List<Producto> findByCategoriaNombre(@Param("categoria") String categoria);

    // =====================================
    // CARGA SIMPLE
    // =====================================

    @EntityGraph(attributePaths = {"categoria"})
    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.id = :id
    """)
    Optional<Producto> findByIdWithCategoria(@Param("id") Long id);

    // =====================================
    // index
    // =====================================
    @Query("""
    		SELECT new com.ecomerce.store.dto.producto.publico.ProductoCardDTO(
    		    p.id,
    		    p.productName,
    		    p.price,

    		    COALESCE(
    		        (SELECT MIN(v.precio)
    		         FROM ProductoVariante v
    		         WHERE v.producto.id = p.id),
    		        p.price
    		    ),

    		    CASE WHEN SIZE(p.variantes) > 0 THEN true ELSE false END,

    		    p.tienePromocion,
    		    p.porcentajeDescuento,

    		    COALESCE(
    		        (SELECT i.imageUrl
    		         FROM ImagenProducto i
    		         WHERE i.producto.id = p.id
    		         ORDER BY i.principal DESC, i.orden ASC
    		         LIMIT 1),
    		        '/img/default.png'
    		    ),

    		    c.nombre,
    		    m.nombre,
    		    p.stockSimple
    		)
    		FROM Producto p
    		LEFT JOIN p.categoria c
    		LEFT JOIN p.marca m
    		WHERE p.visibleEnMenu = true
    		ORDER BY p.id DESC
    		""")
    		List<ProductoCardDTO> findProductosIndexOptimizado();
    // =====================================
    // NOMBRES CATEGORIAS VISIBLES
    // =====================================

    @Query("""
        SELECT DISTINCT p.categoria.nombre
        FROM Producto p
        WHERE p.visibleEnMenu = true
        ORDER BY p.categoria.nombre
    """)
    List<String> obtenerNombresCategoriasVisibles();

    // =====================================
    // CARGA COMPLETA
    // =====================================

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.marca
        LEFT JOIN FETCH p.categoria
        LEFT JOIN FETCH p.imagenes
        LEFT JOIN FETCH p.variantes v
        LEFT JOIN FETCH v.atributos
        WHERE p.visibleEnMenu = true
    """)
    List<Producto> findProductosVisiblesConTodo();

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.marca
        LEFT JOIN FETCH p.categoria
        LEFT JOIN FETCH p.imagenes
        LEFT JOIN FETCH p.variantes v
        LEFT JOIN FETCH v.atributos
        WHERE p.id = :id
    """)
    Optional<Producto> findByIdConTodo(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.marca
        LEFT JOIN FETCH p.categoria
        LEFT JOIN FETCH p.imagenes
        WHERE p.productName = :name
    """)
    Optional<Producto> findByProductNameConTodo(@Param("name") String name);

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.variantes v
        LEFT JOIN FETCH v.atributos
        WHERE p.id = :id
    """)
    Producto findByIdWithVariantes(@Param("id") Long id);

    // =====================================
    // LOCK STOCK
    // =====================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.id = :id
    """)
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    // =====================================
    // ADMIN
    // =====================================

    @Modifying
    @Query("""
        UPDATE Producto p
        SET p.visibleEnMenu = :visible
        WHERE p.categoria.id = :categoriaId
    """)
    void updateVisibilidadPorCategoria(
            @Param("categoriaId") Long categoriaId,
            @Param("visible") boolean visible
    );

    // =====================================
    // REPORTES
    // =====================================

    @Query(value = """
        SELECT
            p.id,
            p.product_name,
            p.price,

            COALESCE(
                (
                    SELECT MIN(v.precio)
                    FROM producto_variantes v
                    WHERE v.producto_id = p.id
                ),
                p.price
            ) AS precio_minimo,

            CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM producto_variantes v
                    WHERE v.producto_id = p.id
                )
                THEN true
                ELSE false
            END AS tiene_variantes,

            p.tiene_promocion,
            c.nombre AS categoria_nombre

        FROM productos p
        LEFT JOIN categorias c ON c.id = p.categoria_id
        WHERE p.visible_en_menu = 1
    """, nativeQuery = true)
    List<Object[]> findProductosPrecioRaw();
    
    
    @Query("""
    		SELECT DISTINCT p
    		FROM Producto p
    		LEFT JOIN FETCH p.categoria c
    		LEFT JOIN FETCH p.marca m
    		LEFT JOIN FETCH p.imagenes i
    		WHERE p.visibleEnMenu = true
    		""")
    		List<Producto> findProductosAdminBase();
    
    @Query("""
    		SELECT DISTINCT p
    		FROM Producto p
    		LEFT JOIN FETCH p.variantes v
    		LEFT JOIN FETCH v.atributos a
    		WHERE p.id IN :ids
    		""")
    		List<Producto> findProductosConVariantes(@Param("ids") List<Long> ids);
    
    
    
}