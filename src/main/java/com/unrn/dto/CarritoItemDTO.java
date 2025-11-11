package com.unrn.dto;

import java.math.BigDecimal;

public record CarritoItemDTO(
    String id,
    Integer peliculaId,
    String tituloSnapshot,
    BigDecimal precioUnitario,
    int cantidad
) {}

