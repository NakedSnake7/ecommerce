package com.ecomerce.store.dto;

import java.util.List;

public class CategoriaGrupoDTO {

    private Long id;
    private String nombre;
    private List<ProductoDTO> productos;
    private boolean todosVisibles;

    public CategoriaGrupoDTO(String nombre, List<ProductoDTO> productos) {
        this.nombre = nombre;
        this.productos = productos;
        this.todosVisibles =
                productos.stream().allMatch(ProductoDTO::isVisibleEnMenu);
    }

    public String getNombre() {
        return nombre;
    }

    public List<ProductoDTO> getProductos() {
        return productos;
    }

    public boolean isTodosVisibles() {
        return todosVisibles;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
}