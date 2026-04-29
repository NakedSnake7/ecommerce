package com.ecomerce.store.mapper;

import com.ecomerce.store.dto.producto.admin.ProductoAdminDTO;
import com.ecomerce.store.dto.producto.publico.ProductoCardDTO;
import com.ecomerce.store.dto.producto.publico.ProductoDetailDTO;
import com.ecomerce.store.model.Producto;

public final class ProductoMapper {

    public static ProductoCardDTO toCard(Producto p) {
        return ProductoCardMapper.toDTO(p);
    }

    public static ProductoDetailDTO toDetail(Producto p) {
        return ProductoDetailMapper.toDTO(p);
    }

    public static ProductoAdminDTO toAdmin(Producto p) {
        return ProductoAdminMapper.toDTO(p);
    }
}