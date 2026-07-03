package com.unrn.services;

import com.unrn.event.CompraEventPublisher;
import com.unrn.event.dto.CompraEventDTO;
import com.unrn.event.dto.ItemCompraEventDTO;
import com.unrn.model.*;
import com.unrn.repository.*;
import com.unrn.services.Externo.*;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class CarritoServicio {

    private final CarritoRepositorio repo;
    private final ClientePeliculas clientePeliculas;
    private final CompraEventPublisher compraEventPublisher;

    public CarritoServicio(
            CarritoRepositorio repo,
            ClientePeliculas clientePeliculas,
            CompraEventPublisher compraEventPublisher) {

        this.repo = repo;
        this.clientePeliculas = clientePeliculas;
        this.compraEventPublisher = compraEventPublisher;
    }

    public Carrito crear(String usuarioId) {
        Carrito c = new Carrito();
        c.setUsuarioId(usuarioId);
        return repo.save(c);
    }

    public Carrito obtenerCarritoAbierto(String usuarioId) {

        return repo.findByUsuarioIdAndEstado(
                usuarioId,
                CarritoEstado.ABIERTO).orElseGet(() -> {

                    Carrito carrito = new Carrito();

                    carrito.setUsuarioId(usuarioId);

                    return repo.save(carrito);

                });

    }

    public Carrito agregarItem(String carritoId, Integer peliculaId, int cantidad) {

        Carrito c = obtener(carritoId);
        asegurarEditable(c);

        var p = clientePeliculas.obtenerPorId(peliculaId);

        CarritoItem item = new CarritoItem();
        item.setPeliculaId(p.peliculaId());
        item.setTituloSnapshot(p.titulo());
        item.setImagenUrl(p.imagenAmpliada());
        item.setPrecioUnitario(p.precio());
        item.setCantidad(cantidad);

        c.agregarItem(item);

        return repo.save(c);
    }

    public Carrito actualizarCantidad(
            String carritoId,
            Integer peliculaId,
            int cantidad) {

        Carrito c = obtener(carritoId);

        asegurarEditable(c);

        c.actualizarCantidad(peliculaId, cantidad);

        return repo.save(c);
    }

    public Carrito eliminarItem(
            String carritoId,
            Integer peliculaId) {

        Carrito c = obtener(carritoId);

        asegurarEditable(c);

        c.eliminarItem(peliculaId);

        return repo.save(c);
    }

    public Carrito checkout(String carritoId) {

        Carrito c = obtener(carritoId);

        if (c.getItems().isEmpty()) {
            throw new IllegalStateException("Carrito vacío");
        }

        CompraEventDTO evento = new CompraEventDTO();

        evento.setUsuarioId(c.getUsuarioId());

        evento.setTotal(c.getTotal());

        evento.setItems(
                c.getItems()
                        .stream()
                        .map(item -> {

                            ItemCompraEventDTO dto = new ItemCompraEventDTO();

                            dto.setPeliculaId(item.getPeliculaId());
                            dto.setTituloSnapshot(item.getTituloSnapshot());
                            dto.setImagenUrl(item.getImagenUrl());
                            dto.setPrecioUnitario(item.getPrecioUnitario());
                            dto.setCantidad(item.getCantidad());

                            return dto;
                        })
                        .toList());

        try {
            compraEventPublisher.enviarEvento(evento);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        c.setEstado(CarritoEstado.CONFIRMADO);

        return repo.save(c);

    }

    public Carrito asociarUsuario(String carritoId, String usuarioId) {

        Carrito carrito = obtener(carritoId);

        carrito.setUsuarioId(usuarioId);

        return repo.save(carrito);
    }

    public Carrito asociarOFusionar(String carritoAnonimoId, String usuarioId) {

        Carrito carritoAnonimo = obtener(carritoAnonimoId);

        Optional<Carrito> carritoUsuarioOpt = repo.findByUsuarioIdAndEstado(usuarioId, CarritoEstado.ABIERTO);

        if (carritoUsuarioOpt.isEmpty()) {

            carritoAnonimo.setUsuarioId(usuarioId);

            return repo.save(carritoAnonimo);
        }

        Carrito carritoUsuario = carritoUsuarioOpt.get();

        for (CarritoItem itemAnonimo : carritoAnonimo.getItems()) {

            CarritoItem nuevoItem = new CarritoItem();

            nuevoItem.setPeliculaId(itemAnonimo.getPeliculaId());
            nuevoItem.setTituloSnapshot(itemAnonimo.getTituloSnapshot());
            nuevoItem.setImagenUrl(itemAnonimo.getImagenUrl());
            nuevoItem.setPrecioUnitario(itemAnonimo.getPrecioUnitario());
            nuevoItem.setCantidad(itemAnonimo.getCantidad());

            carritoUsuario.agregarItem(nuevoItem);
        }

        repo.delete(carritoAnonimo);

        return repo.save(carritoUsuario);
    }

    public Carrito obtener(String idCarrito) {
        return repo.findById(idCarrito).orElseThrow();
    }

    private void asegurarEditable(Carrito c) {

        if (c.getEstado() != CarritoEstado.ABIERTO) {
            throw new IllegalStateException("El carrito no es editable en estado " + c.getEstado());
        }
    }
}