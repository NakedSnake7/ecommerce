package com.ecomerce.store.model;

import org.springframework.web.multipart.MultipartFile;

public class Resena {
	private Long id; 
    private String nombre;
    private MultipartFile imagen;        // Archivo subido
    private String comentario;
    private Integer estrellas;
    private boolean verificado;

    // Para Cloudinary
    private String imagenUrl;
    private String publicId;

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public MultipartFile getImagen() { return imagen; }
    public void setImagen(MultipartFile imagen) { this.imagen = imagen; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Integer getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(Integer estrellas) {
        this.estrellas = estrellas;
    }
    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
