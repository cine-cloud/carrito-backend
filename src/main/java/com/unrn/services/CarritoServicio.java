package com.unrn.services;

import com.unrn.model.*;
import com.unrn.repository.*;
import com.unrn.services.Externo.*;
import com.unrn.services.mensajeria.*;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CarritoServicio {

  private final CarritoRepositorio repo;
  private final ClientePeliculas clientePeliculas;
  private final MensajeriaStock mensajeria;

  public CarritoServicio(CarritoRepositorio repo, ClientePeliculas clientePeliculas, MensajeriaStock mensajeria) {
    this.repo = repo; this.clientePeliculas = clientePeliculas; this.mensajeria = mensajeria;
  }

  public Carrito crear(String usuarioId) {
    Carrito c = new Carrito();
    c.setUsuarioId(usuarioId);
    return repo.save(c);
  }

  public Carrito agregarItem(String carritoId, Integer peliculaId, int cantidad) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);

    var p = clientePeliculas.obtenerPorId(peliculaId);

    CarritoItem item = new CarritoItem();
    item.setPeliculaId(p.peliculaId());
    item.setTituloSnapshot(p.titulo());
    item.setPrecioUnitario(p.precio());
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
    if (c.getItems().isEmpty()) throw new IllegalStateException("Carrito vacío");
    c.setEstado(CarritoEstado.PENDIENTE_STOCK);
    repo.save(c);
    mensajeria.solicitarReserva(c);
    return c;
  }

  public Carrito obtener(String id) {
    return repo.findById(id).orElseThrow();
  }

  // llamados por el listener AMQP
  public void onStockReservado(String carritoId) {
    Carrito c = obtener(carritoId);
    c.setEstado(CarritoEstado.STOCK_RESERVADO);
    repo.save(c);
  }
  public void onStockRechazado(String carritoId, String motivo) {
    Carrito c = obtener(carritoId);
    c.setEstado(CarritoEstado.ABIERTO); // o CANCELADO según negocio
    repo.save(c);
  }

  private void asegurarEditable(Carrito c) {
    if (c.getEstado() != CarritoEstado.ABIERTO)
      throw new IllegalStateException("El carrito no es editable en estado " + c.getEstado());
  }
}

