package com.unrn.carritos.controllercontroller;

import com.unrn.carritos.DTO.*;
import com.unrn.carritos.model.Carrito;
import com.unrn.carritos.service.CarritoServicio;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carritos")
public class CarritoControlador {

  private final CarritoServicio servicio;
  public CarritoControlador(CarritoServicio servicio) { this.servicio = servicio; }

  @PostMapping("/{usuarioId}")
  public Carrito crearCarrito(@PathVariable String usuarioId) {
    return servicio.crear(usuarioId);
  }

  @GetMapping("/{idCarrito}")
  public Carrito obtenerCarrito(@PathVariable String idCarrito) 
    { return servicio.obtener(idCarrito); }

  @PostMapping("/agregar-item/{idCarrito}")
  public Carrito agregarItemCarrito(@PathVariable String idCarrito, @Valid @RequestBody AgregarItemDTO dto) {
    return servicio.agregarItem(idCarrito, dto.peliculaId(), dto.cantidad());
  }
 
}