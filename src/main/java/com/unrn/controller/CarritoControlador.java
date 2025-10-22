package com.unrn.controller;

import com.unrn.controller.DTO.*;
import com.unrn.model.Carrito;
import com.unrn.services.CarritoServicio;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carritos")
public class CarritoControlador {

  private final CarritoServicio servicio;
  public CarritoControlador(CarritoServicio servicio) { this.servicio = servicio; }

  @PostMapping
  public Carrito crear(@RequestHeader("X-Usuario-Id") String usuarioId) {
    return servicio.crear(usuarioId);
  }

  @GetMapping("/{id}")
  public Carrito obtener(@PathVariable String id) { return servicio.obtener(id); }

  @PostMapping("/{id}/items")
  public Carrito agregarItem(@PathVariable String id, @Valid @RequestBody AgregarItemDTO dto) {
    return servicio.agregarItem(id, dto.peliculaId(), dto.cantidad());
  }

  @PatchMapping("/{id}/items/{peliculaId}")
  public Carrito actualizar(@PathVariable String id, @PathVariable Integer peliculaId,
                            @Valid @RequestBody ActualizarCantidadDTO dto) {
    return servicio.actualizarCantidad(id, peliculaId, dto.cantidad());
  }

  @DeleteMapping("/{id}/items/{peliculaId}")
  public Carrito eliminar(@PathVariable String id, @PathVariable Integer peliculaId) {
    return servicio.eliminarItem(id, peliculaId);
  }

  @PostMapping("/{id}/checkout")
  public Carrito checkout(@PathVariable String id) { return servicio.checkout(id); }
}

