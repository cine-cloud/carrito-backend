package com.unrn.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
public class Carrito {

  @Id
  @Column(name = "carrito_id", length = 36)
  private String id = UUID.randomUUID().toString();

  @Column(name="usuario_id")
  private String usuarioId;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private CarritoEstado estado = CarritoEstado.ABIERTO;

  @Column(nullable=false, precision=12, scale=2)
  private BigDecimal total = BigDecimal.ZERO;

  @OneToMany(mappedBy="carrito", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
  private List<CarritoItem> items = new ArrayList<>();

  public void agregarItem(CarritoItem item) {

    CarritoItem existente = items.stream()
            .filter(i -> i.getPeliculaId().equals(item.getPeliculaId()))
            .findFirst()
            .orElse(null);

    if (existente != null) {

        existente.setCantidad(
                existente.getCantidad() + item.getCantidad()
        );

    } else {

        item.setCarrito(this);
        items.add(item);
    }

    recalcular();
  }

  public void eliminarItem(Integer peliculaId) {
    items.removeIf(i -> i.getPeliculaId().equals(peliculaId));
    recalcular();
  }

  public void actualizarCantidad(Integer peliculaId, int cantidad) {
    items.stream().filter(i -> i.getPeliculaId().equals(peliculaId)).findFirst()
      .ifPresent(i -> { i.setCantidad(cantidad); recalcular(); });
  }

  public void recalcular() {
    total = items.stream()
      .map(i -> i.getPrecioUnitario().multiply(new BigDecimal(i.getCantidad())))
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}