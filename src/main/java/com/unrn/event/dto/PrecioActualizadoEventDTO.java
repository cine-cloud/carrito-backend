package com.unrn.event.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PrecioActualizadoEventDTO {

    private Integer peliculaId;
    private BigDecimal nuevoPrecio;
}
