package com.unrn.carritos.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "peliculas")
public class Pelicula {
    @Id
    @Column(name = "pelicula_id", nullable = false)
    private Integer peliculaId;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, length = 50)
    private String condicion;

    @Column(nullable = false, length = 50)
    private String formato;

    @Column(columnDefinition = "TEXT")
    private String sinopsis;

    @Column(name = "imagen_ampliada", length = 255)
    private String imagenAmpliada;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate = LocalDateTime.now();

}
