package com.unrn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PeliculaEventDTO(
    Integer peliculaId,
    String titulo,
    BigDecimal precio
) {}

