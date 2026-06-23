package com.unrn.event.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompraEventDTO {

    private String usuarioId;

    private String fechaTransaccion;

    private BigDecimal total;

    private List<ItemCompraEventDTO> items;
}