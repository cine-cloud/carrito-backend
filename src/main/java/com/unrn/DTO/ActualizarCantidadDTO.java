package com.unrn.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActualizarCantidadDTO(
        @NotNull Integer peliculaId,
        @Positive int cantidad
) {}