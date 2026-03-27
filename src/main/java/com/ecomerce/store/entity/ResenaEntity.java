package com.ecomerce.store.entity;

import org.hibernate.annotations.DynamicUpdate;   

import jakarta.persistence.*;

@DynamicUpdate
@Entity
@Table(name = "resenas")
public class ResenaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(length = 1000)
    private String comentario;

    private int estrellas;

    private boolean verificado;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "public_id")
    private String publicId;

    //  GETTERS Y SETTERS

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public int getEstrellas() { return estrellas; }
    public void setEstrellas(int estrellas) { this.estrellas = estrellas; }

    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
}
