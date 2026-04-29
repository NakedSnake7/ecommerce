package com.ecomerce.store.dto.producto.publico;

import java.util.Collections;
import java.util.List;

public class CategoriaGrupoCardDTO {

    private Long id;
    private String nombre;
    private List<ProductoCardDTO> productos;
    private boolean todosVisibles;

    public CategoriaGrupoCardDTO() {
    }

    public CategoriaGrupoCardDTO(
            Long id,
            String nombre,
            List<ProductoCardDTO> productos
    ) {
        this.id = id;
        this.nombre = nombre;
        setProductos(productos);
    }

    private boolean calcularTodosVisibles(List<ProductoCardDTO> productos) {
        return productos != null
                && !productos.isEmpty()
                && productos.stream()
                        .allMatch(ProductoCardDTO::isVisibleEnMenu);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<ProductoCardDTO> getProductos() {
        return productos;
    }

    public boolean isTodosVisibles() {
        return todosVisibles;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setProductos(List<ProductoCardDTO> productos) {
        this.productos = productos != null
                ? productos
                : Collections.emptyList();

        this.todosVisibles =
                calcularTodosVisibles(this.productos);
    }

    public void setTodosVisibles(boolean todosVisibles) {
        this.todosVisibles = todosVisibles;
    }
}