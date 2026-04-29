package com.ecomerce.store.dto.producto.admin;

import java.util.List;

public class CategoriaGrupoAdminDTO {

    private Long id;
    private String nombre;
    private List<ProductoAdminListDTO> productos;
    private boolean todosVisibles;

    public CategoriaGrupoAdminDTO(
            Long id,
            String nombre,
            List<ProductoAdminListDTO> productos) {

        this.id = id;
        this.nombre = nombre;
        this.productos = productos;

        this.todosVisibles =
            productos.stream()
                     .allMatch(ProductoAdminListDTO::isVisibleEnMenu);
    }

    public Long getId(){ return id; }
    public String getNombre(){ return nombre; }
    public List<ProductoAdminListDTO> getProductos(){ return productos; }
    public boolean isTodosVisibles(){ return todosVisibles; }
}