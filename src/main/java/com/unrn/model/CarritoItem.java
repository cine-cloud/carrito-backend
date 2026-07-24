package com.unrn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter; 
import lombok.Setter; 
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "carrito_items")
@Getter @Setter @NoArgsConstructor
public class CarritoItem {

  @Id
  @Column(name="item_id", length=36)
  private String id = UUID.randomUUID().toString();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="carrito_id", nullable=false)
  @JsonIgnore
  private Carrito carrito;

  @Column(name="pelicula_id", nullable=false)
  private Integer peliculaId;

  @Column(name="titulo_snapshot", nullable=false, length=255)
  private String tituloSnapshot;

  @Column(name = "imagen_url")
  private String imagenUrl;

  @Column(name="precio_unitario", nullable=false, precision=12, scale=2)
  private BigDecimal precioUnitario;

  @Column(nullable=false)
  private int cantidad;
}
