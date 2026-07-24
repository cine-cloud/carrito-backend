package com.unrn.DTO;

import jakarta.validation.constraints.NotNull;

public record EliminarItemDTO(
        @NotNull Integer peliculaId
) {}
