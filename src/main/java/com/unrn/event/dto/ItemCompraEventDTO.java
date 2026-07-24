package com.unrn.event.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemCompraEventDTO {

    private Integer peliculaId;

    private String imagenUrl;

    private String tituloSnapshot;

    private BigDecimal precioUnitario;

    private Integer cantidad;
}