package com.unrn.services.mensajeria;

import java.util.List;

public record ComandoReservarStock(
    String carritoId,
    List<Item> items
) {
    public record Item(Integer peliculaId, int cantidad) {}
}
