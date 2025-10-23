package com.unrn.services;

import com.unrn.model.*;
import com.unrn.repository.*;
import com.unrn.services.Externo.*;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CarritoServicio {

  private final CarritoRepositorio repo;
  private final ClientePeliculas clientePeliculas;

  public CarritoServicio(CarritoRepositorio repo, ClientePeliculas clientePeliculas) {
    this.repo = repo; 
    this.clientePeliculas = clientePeliculas;
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
    c.setEstado(CarritoEstado.CONFIRMADO);
    return repo.save(c);
  }

  public Carrito obtener(String idCarrito) {
    return repo.findById(idCarrito).orElseThrow();
  }

  private void asegurarEditable(Carrito c) {
    if (c.getEstado() != CarritoEstado.ABIERTO)
      throw new IllegalStateException("El carrito no es editable en estado " + c.getEstado());
  }
}

