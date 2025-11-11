package com.unrn.services;

import com.unrn.model.*;
import com.unrn.repository.*;
import com.unrn.dto.*;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarritoServicio {

  private final CarritoRepositorio repo;
  private final RestTemplate restTemplate;
  private final String peliculasBaseUrl;

  public CarritoServicio(CarritoRepositorio repo, 
                        @Value("${peliculas.base-url}") String peliculasBaseUrl) {
    this.repo = repo;
    this.restTemplate = new RestTemplate();
    this.peliculasBaseUrl = peliculasBaseUrl;
  }

  public CarritoDTO crear(String usuarioId) {
    Carrito c = new Carrito();
    c.setUsuarioId(usuarioId);
    return toDTO(repo.save(c));
  }

  public CarritoDTO agregarItem(String carritoId, Integer peliculaId, int cantidad) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);

    // Obtener información de la película desde el microservicio
    var resp = restTemplate.getForEntity(peliculasBaseUrl + "/api/peliculas/{id}", 
        PeliculaResponse.class, peliculaId);
    
    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
      throw new IllegalStateException("No se pudo obtener Película " + peliculaId);
    }
    
    var p = resp.getBody();

    CarritoItem item = new CarritoItem();
    item.setPeliculaId(p.peliculaId());
    item.setTituloSnapshot(p.titulo());
    item.setPrecioUnitario(p.precio());
    item.setCantidad(cantidad);
    c.agregarItem(item);

    return toDTO(repo.save(c));
  }
  
  private record PeliculaResponse(Integer peliculaId, String titulo, BigDecimal precio) {}

  public CarritoDTO actualizarCantidad(String carritoId, Integer peliculaId, int cantidad) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);
    c.actualizarCantidad(peliculaId, cantidad);
    return toDTO(repo.save(c));
  }

  public CarritoDTO eliminarItem(String carritoId, Integer peliculaId) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);
    c.eliminarItem(peliculaId);
    return toDTO(repo.save(c));
  }

  public CarritoDTO checkout(String carritoId) {
    Carrito c = obtener(carritoId);
    if (c.getItems().isEmpty()) throw new IllegalStateException("Carrito vacío");
    c.setEstado(CarritoEstado.CONFIRMADO);
    return toDTO(repo.save(c));
  }

  public Carrito obtener(String idCarrito) {
    return repo.findById(idCarrito)
        .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado con ID: " + idCarrito));
  }

  public void eliminar(String carritoId) {
    Carrito c = obtener(carritoId);
    if (c.getEstado() == CarritoEstado.CONFIRMADO) {
      throw new IllegalStateException("No se puede eliminar un carrito confirmado");
    }
    repo.delete(c);
  }

  public CarritoDTO vaciar(String carritoId) {
    Carrito c = obtener(carritoId);
    asegurarEditable(c);
    c.getItems().clear();
    c.recalcular();
    return toDTO(repo.save(c));
  }

  public CarritoDTO cancelar(String carritoId) {
    Carrito c = obtener(carritoId);
    c.setEstado(CarritoEstado.CANCELADO);
    return toDTO(repo.save(c));
  }

  public java.util.List<CarritoDTO> listarPorUsuario(String usuarioId) {
    return repo.findByUsuarioId(usuarioId).stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  private void asegurarEditable(Carrito c) {
    if (c.getEstado() != CarritoEstado.ABIERTO)
      throw new IllegalStateException("El carrito no es editable en estado " + c.getEstado());
  }

  public CarritoDTO toDTO(Carrito c) {
    return new CarritoDTO(
        c.getId(),
        c.getUsuarioId(),
        c.getEstado(),
        c.getTotal(),
        c.getItems().stream()
            .map(item -> new CarritoItemDTO(
                item.getId(),
                item.getPeliculaId(),
                item.getTituloSnapshot(),
                item.getPrecioUnitario(),
                item.getCantidad()
            ))
            .collect(Collectors.toList())
    );
  }
}

