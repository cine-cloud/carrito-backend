package com.unrn.dto;

import jakarta.validation.constraints.*;

public record ActualizarCantidadDTO(@Positive int cantidad) {}

