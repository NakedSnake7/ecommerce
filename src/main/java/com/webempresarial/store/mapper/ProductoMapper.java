package com.webempresarial.store.mapper;

import com.webempresarial.store.dto.producto.admin.ProductoAdminDTO;
import com.webempresarial.store.dto.producto.publico.ProductoCardDTO;
import com.webempresarial.store.dto.producto.publico.ProductoDetailDTO;
import com.webempresarial.store.model.Producto;

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