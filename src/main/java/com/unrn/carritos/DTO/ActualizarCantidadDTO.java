package com.unrn.carritos.DTO;

import jakarta.validation.constraints.*;

public record ActualizarCantidadDTO(@Positive int cantidad) {
}
