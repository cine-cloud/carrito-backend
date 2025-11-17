package com.unrn.carritos.service;

import com.unrn.carritos.model.*;
import com.unrn.carritos.repository.*;
import com.unrn.carritos.domain.Pelicula;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CarritoServicio {

  private final CarritoRepositorio repo;
  private final PeliculaRepository peliculaRepository;

  public CarritoServicio(CarritoRepositorio repo, PeliculaRepository peliculaRepository) {
    this.repo = repo;
    this.peliculaRepository = peliculaRepository;
  }

  public Carrito crear(String usuarioId) {
    Carrito c = new Carrito();
    c.setUsuarioId(usuarioId);
    return repo.save(c);
  }

  public Carrito agregarItem(String carritoId, Integer peliculaId, int cantidad) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);

    // Obtener película desde la BD local (sincronizada vía RabbitMQ)
    Pelicula p = peliculaRepository.findById(String.valueOf(peliculaId))
        .orElseThrow(() -> new IllegalStateException("Película con ID " + peliculaId + " no encontrada. Asegúrate de que la película exista en el servicio de películas y que RabbitMQ esté sincronizando los datos."));

    CarritoItem item = new CarritoItem();
    item.setPeliculaId(peliculaId);
    item.setTituloSnapshot(p.getTitulo());
    item.setPrecioUnitario(p.getPrecio());
    item.setCantidad(cantidad);
    c.agregarItem(item);

    return repo.save(c);
  }

  public Carrito actualizarCantidad(String carritoId, Integer peliculaId, int cantidad) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);
    c.actualizarCantidad(peliculaId, cantidad);
    return repo.save(c);
  }

  public Carrito eliminarItem(String carritoId, Integer peliculaId) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);
    c.eliminarItem(peliculaId);
    return repo.save(c);
  }

  public Carrito checkout(String carritoId) {
    Carrito c = obtener(carritoId);
    if (c.getItems().isEmpty())
      throw new IllegalStateException("Carrito vacío");
    c.setEstado(CarritoEstado.CONFIRMADO);
    return repo.save(c);
  }

  public Carrito obtener(String idCarrito) {
    return repo.findById(idCarrito).orElseThrow();
  }

  public void eliminar(String carritoId) {
    Carrito c = obtener(carritoId);
    if (c.getEstado() == CarritoEstado.CONFIRMADO) {
      throw new IllegalStateException("No se puede eliminar un carrito confirmado");
    }
    repo.delete(c);
  }

  public Carrito vaciar(String carritoId) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);
    c.getItems().clear();
    c.recalcular();
    return repo.save(c);
  }

  public Carrito cancelar(String carritoId) {
    Carrito c = obtener(carritoId);
    c.setEstado(CarritoEstado.CANCELADO);
    return repo.save(c);
  }

  public java.util.List<Carrito> listarPorUsuario(String usuarioId) {
    return repo.findByUsuarioId(usuarioId);
  }

  private void asegurarEditable(Carrito c) {
    if (c.getEstado() != CarritoEstado.ABIERTO)
      throw new IllegalStateException("El carrito no es editable en estado " + c.getEstado());
  }
}
