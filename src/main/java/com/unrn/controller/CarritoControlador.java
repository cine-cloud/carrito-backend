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

  @PostMapping("/crear-carrito/{usuarioId}")
  public Carrito crearCarrito(@PathVariable String usuarioId) {
    return servicio.crear(usuarioId);
  }

  @GetMapping("/obtener-carrito/{idCarrito}")
  public Carrito obtenerCarrito(@PathVariable String idCarrito) 
    { return servicio.obtener(idCarrito); }

  @PostMapping("/agregar-item/{idCarrito}")
  public Carrito agregarItemCarrito(@PathVariable String idCarrito, @Valid @RequestBody AgregarItemDTO dto) {
    return servicio.agregarItem(idCarrito, dto.peliculaId(), dto.cantidad());
  }
 
}

