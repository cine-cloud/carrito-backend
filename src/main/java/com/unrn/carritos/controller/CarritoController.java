package com.unrn.carritos.controller;

import com.unrn.carritos.DTO.*;
import com.unrn.carritos.model.Carrito;
import com.unrn.carritos.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carritos")
public class CarritoController {

  private final CarritoService servicio;

  public CarritoController(CarritoService servicio) {
    this.servicio = servicio;
  }

  @PostMapping("/{usuarioId}")
  public Carrito crearCarrito(@PathVariable String usuarioId) {
    return servicio.crear(usuarioId);
  }

  @GetMapping("/{idCarrito}")
  public Carrito obtenerCarrito(@PathVariable String idCarrito) {
    return servicio.obtener(idCarrito);
  }

  @PostMapping("/agregar-item/{idCarrito}")
  public Carrito agregarItemCarrito(@PathVariable String idCarrito, @Valid @RequestBody AgregarItemDTO dto) {
    return servicio.agregarItem(idCarrito, dto.peliculaId(), dto.cantidad());
  }

  @DeleteMapping("/{idCarrito}/items/{peliculaId}")
  public Carrito eliminarItem(@PathVariable String idCarrito, @PathVariable Integer peliculaId) {
    return servicio.eliminarItem(idCarrito, peliculaId);
  }

  @DeleteMapping("/{idCarrito}/items")
  public Carrito vaciarCarrito(@PathVariable String idCarrito) {
    return servicio.vaciar(idCarrito);
  }

}