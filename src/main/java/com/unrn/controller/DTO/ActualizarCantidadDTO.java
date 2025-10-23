package com.unrn.controller.DTO;

import jakarta.validation.constraints.*;

public record ActualizarCantidadDTO(@Positive int cantidad) {}
