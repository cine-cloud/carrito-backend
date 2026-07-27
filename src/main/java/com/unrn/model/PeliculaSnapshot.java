package com.unrn.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pelicula_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PeliculaSnapshot {

    @Id
    @Column(name = "pelicula_id")
    private Integer peliculaId;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "imagen_ampliada")
    private String imagenAmpliada;

    private Integer stock;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
}
