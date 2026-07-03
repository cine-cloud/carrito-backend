package com.unrn.controller;

import com.unrn.DTO.*;
import com.unrn.model.Carrito;
import com.unrn.services.CarritoServicio;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carritos")
@CrossOrigin(origins = "http://localhost:3000")
public class CarritoControlador {

  private final CarritoServicio servicio;

  public CarritoControlador(CarritoServicio servicio) {
    this.servicio = servicio;
  }

  @PostMapping("/{usuarioId}")
  public Carrito crearCarrito(@PathVariable String usuarioId) {
    return servicio.crear(usuarioId);
  }

  @GetMapping("/usuario/{usuarioId}")
  public Carrito obtenerCarritoUsuario(
      @PathVariable String usuarioId) {
    return servicio.obtenerCarritoAbierto(usuarioId);
  }

  @PostMapping
  public Carrito crearCarritoAnonimo() {
    return servicio.crear(null);
  }

  @GetMapping("/{idCarrito}")
  public Carrito obtenerCarrito(@PathVariable String idCarrito) {
    return servicio.obtener(idCarrito);
  }

  @PostMapping("/agregar-item/{idCarrito}")
  public Carrito agregarItemCarrito(@PathVariable String idCarrito, @Valid @RequestBody AgregarItemDTO dto) {
    return servicio.agregarItem(idCarrito, dto.peliculaId(), dto.cantidad());
  }

  @PutMapping("/{idCarrito}/fusionar/{usuarioId}")
  public Carrito asociarOFusionar(@PathVariable String idCarrito, @PathVariable String usuarioId) {
    return servicio.asociarOFusionar(idCarrito, usuarioId);
  }

  @PostMapping("/checkout/{idCarrito}")
  public Carrito checkout(@PathVariable String idCarrito) {
    return servicio.checkout(idCarrito);
  }

  @DeleteMapping("/eliminar-item/{idCarrito}")
  public Carrito eliminarItem(@PathVariable String idCarrito, @Valid @RequestBody EliminarItemDTO dto) {
    return servicio.eliminarItem(idCarrito, dto.peliculaId());
  }

  @PutMapping("/actualizar-cantidad/{idCarrito}")
  public Carrito actualizarCantidad(@PathVariable String idCarrito, @Valid @RequestBody ActualizarCantidadDTO dto) {
    return servicio.actualizarCantidad(idCarrito, dto.peliculaId(), dto.cantidad());
  }
}