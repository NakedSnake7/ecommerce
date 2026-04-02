package com.ecomerce.store.mapper;

import com.ecomerce.store.entity.ResenaEntity;     
import com.ecomerce.store.model.Resena;

public class ResenaMapper {

    public static ResenaEntity toEntity(Resena r) {
        ResenaEntity e = new ResenaEntity();
        e.setNombre(r.getNombre());
        e.setComentario(r.getComentario());
        e.setEstrellas(r.getEstrellas());
        e.setVerificado(r.isVerificado());
        e.setImagenUrl(r.getImagenUrl());
        e.setPublicId(r.getPublicId());
        return e;
    }
    
    public static Resena toModel(ResenaEntity e) {
        Resena r = new Resena();
        r.setId(e.getId());
        r.setNombre(e.getNombre());
        r.setComentario(e.getComentario());
        r.setEstrellas(e.getEstrellas());
        r.setVerificado(e.isVerificado());
        r.setImagenUrl(e.getImagenUrl());
        r.setPublicId(e.getPublicId());
        return r;
    }
    
}
