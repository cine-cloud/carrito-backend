package com.unrn.controller.DTO;

import jakarta.validation.constraints.*;

public record AgregarItemDTO(@NotNull Integer peliculaId, @Positive int cantidad) {}

public record ActualizarCantidadDTO(@Positive int cantidad) {}

