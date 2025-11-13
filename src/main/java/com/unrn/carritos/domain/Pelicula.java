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
    private String peliculaId; // Cambiado a String para que coincida con event.getKey()

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false)
    private LocalDate fechaSalida;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, length = 50)
    private String condicion;

    @Column(nullable = false, length = 50)
    private String formato;

    @Column(columnDefinition = "TEXT")
    private String sinopsis;

    @Column(length = 255)
    private String imagenAmpliada;

    @Column(nullable = false)
    private LocalDateTime lastUpdate = LocalDateTime.now();

}
