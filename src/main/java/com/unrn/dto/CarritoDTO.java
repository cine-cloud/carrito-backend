package com.unrn.dto;

import com.unrn.model.CarritoEstado;
import java.math.BigDecimal;
import java.util.List;

public record CarritoDTO(
    String id,
    String usuarioId,
    CarritoEstado estado,
    BigDecimal total,
    List<CarritoItemDTO> items
) {}

