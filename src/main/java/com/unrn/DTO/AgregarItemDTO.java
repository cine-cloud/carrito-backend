package com.unrn.DTO;

import jakarta.validation.constraints.*;

public record AgregarItemDTO(@NotNull Integer peliculaId, @Positive int cantidad) {}

