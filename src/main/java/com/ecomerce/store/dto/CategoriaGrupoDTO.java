package com.ecomerce.store.dto;

import java.util.List;  

import com.ecomerce.store.model.Producto;

public class CategoriaGrupoDTO {

    private String nombre;
    private List<Producto> productos;
    private boolean todosVisibles;

    public CategoriaGrupoDTO(String nombre, List<Producto> productos) {
        this.nombre = nombre;
        this.productos = productos;
        this.todosVisibles =
                productos.stream().allMatch(Producto::isVisibleEnMenu);
    }

    public String getNombre() {
        return nombre;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public boolean isTodosVisibles() {
        return todosVisibles;
    }
}
