package com.unrn.dto;

import jakarta.validation.constraints.*;

public record AgregarItemDTO(@NotNull Integer peliculaId, @Positive int cantidad) {}

