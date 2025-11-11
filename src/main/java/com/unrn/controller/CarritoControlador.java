package com.unrn.controller;

import com.unrn.dto.*;
import com.unrn.services.CarritoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carrito")
public class CarritoControlador {

  private final CarritoServicio servicio;
  public CarritoControlador(CarritoServicio servicio) { this.servicio = servicio; }

  // Crear carrito
  @PostMapping("/{usuarioId}")
  @ResponseStatus(HttpStatus.CREATED)
  public CarritoDTO crearCarrito(@PathVariable String usuarioId) {
    return servicio.crear(usuarioId);
  }

  // Obtener un carrito
  @GetMapping("/{idCarrito}")
  public CarritoDTO obtenerCarrito(@PathVariable String idCarrito) {
    return servicio.toDTO(servicio.obtener(idCarrito));
  }

  // Listar carritos de un usuario
  @GetMapping("/usuario/{usuarioId}")
  public List<CarritoDTO> listarCarritosPorUsuario(@PathVariable String usuarioId) {
    return servicio.listarPorUsuario(usuarioId);
  }

  // Eliminar carrito
  @DeleteMapping("/{idCarrito}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminarCarrito(@PathVariable String idCarrito) {
    servicio.eliminar(idCarrito);
  }

  // Agregar item al carrito
  @PostMapping("/{idCarrito}/items")
  public CarritoDTO agregarItem(@PathVariable String idCarrito, @Valid @RequestBody AgregarItemDTO dto) {
    return servicio.agregarItem(idCarrito, dto.peliculaId(), dto.cantidad());
  }

  // Actualizar cantidad de un item
  @PutMapping("/{idCarrito}/items/{peliculaId}")
  public CarritoDTO actualizarCantidad(
      @PathVariable String idCarrito,
      @PathVariable Integer peliculaId,
      @Valid @RequestBody ActualizarCantidadDTO dto) {
    return servicio.actualizarCantidad(idCarrito, peliculaId, dto.cantidad());
  }

  // Eliminar item del carrito
  @DeleteMapping("/{idCarrito}/items/{peliculaId}")
  public CarritoDTO eliminarItem(@PathVariable String idCarrito, @PathVariable Integer peliculaId) {
    return servicio.eliminarItem(idCarrito, peliculaId);
  }

  // Vaciar carrito (eliminar todos los items)
  @DeleteMapping("/{idCarrito}/items")
  public CarritoDTO vaciarCarrito(@PathVariable String idCarrito) {
    return servicio.vaciar(idCarrito);
  }

  // Confirmar carrito (checkout)
  @PostMapping("/{idCarrito}/confirmar")
  public CarritoDTO confirmarCarrito(@PathVariable String idCarrito) {
    return servicio.checkout(idCarrito);
  }

  // Cancelar carrito
  @PostMapping("/{idCarrito}/cancel")
  public CarritoDTO cancelarCarrito(@PathVariable String idCarrito) {
    return servicio.cancelar(idCarrito);
  }
 
}

