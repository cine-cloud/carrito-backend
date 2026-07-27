package com.unrn.services.port;

import java.math.BigDecimal;
import java.util.List;

public interface ClientePeliculasPort {

    PeliculaRemota obtenerPorId(Integer id);
    void descontarStock(DescuentoStockRequest request);

    record PeliculaRemota(
            Integer peliculaId,
            String titulo,
            BigDecimal precio,
            String imagenAmpliada,
            Integer stock
    ) {}

    record DescuentoStockDTO(
            Integer peliculaId,
            Integer cantidad
    ) {}

    record DescuentoStockRequest(
            List<DescuentoStockDTO> peliculas
    ) {}
}
