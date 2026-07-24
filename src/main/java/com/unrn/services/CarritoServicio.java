package com.unrn.services;

import com.unrn.event.CompraEventPublisher;
import com.unrn.event.dto.CompraEventDTO;
import com.unrn.event.dto.ItemCompraEventDTO;
import com.unrn.model.*;
import com.unrn.repository.*;
import com.unrn.services.Externo.*;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

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

    public Carrito checkout(String carritoId, java.math.BigDecimal descuentoMonto) {

        Carrito c = obtener(carritoId);

        if (c.getItems().isEmpty()) {
            throw new IllegalStateException("Carrito vacío");
        }

        ClientePeliculas.DescuentoStockRequest request = new ClientePeliculas.DescuentoStockRequest(

                c.getItems()
                        .stream()
                        .map(item -> new ClientePeliculas.DescuentoStockDTO(
                                item.getPeliculaId(),
                                item.getCantidad()))
                        .toList());

        clientePeliculas.descontarStock(request);

        CompraEventDTO evento = new CompraEventDTO();

        evento.setUsuarioId(c.getUsuarioId());

        // Recuperar información del usuario autenticado vía JWT
        String email = "cliente@ejemplo.com";
        String nombre = "Cliente";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) auth.getPrincipal();
                email = jwt.getClaimAsString("email");
                if (email == null) {
                    email = jwt.getClaimAsString("preferred_username") + "@mail.com";
                }
                nombre = jwt.getClaimAsString("name");
                if (nombre == null) {
                    nombre = jwt.getClaimAsString("preferred_username");
                }
            }
        } catch (Exception ex) {
            // Ignorar y usar fallbacks
        }

        String transactionId = java.util.UUID.randomUUID().toString();
        evento.setIdCompra(transactionId);
        evento.setFecha(java.time.LocalDateTime.now());
        evento.setEmailCliente(email);
        evento.setNombreCliente(nombre);

        java.math.BigDecimal total = c.getTotal();
        if (descuentoMonto != null) {
            total = total.subtract(descuentoMonto);
            if (total.compareTo(java.math.BigDecimal.ZERO) < 0) {
                total = java.math.BigDecimal.ZERO;
            }
            c.setTotal(total);
        }

        evento.setTotal(total);

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

        evento.setProductos(
                c.getItems()
                        .stream()
                        .map(item -> {
                            CompraEventDTO.ProductoDTO prod = new CompraEventDTO.ProductoDTO();
                            prod.setIdProducto(String.valueOf(item.getPeliculaId()));
                            prod.setNombre(item.getTituloSnapshot());
                            prod.setCantidad(item.getCantidad());
                            prod.setPrecioUnitario(item.getPrecioUnitario());
                            prod.setSubtotal(item.getPrecioUnitario().multiply(new java.math.BigDecimal(item.getCantidad())));
                            return prod;
                        })
                        .toList()
        );

        try {
            compraEventPublisher.enviarEvento(evento);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        c.setEstado(CarritoEstado.CONFIRMADO);

        return repo.save(c);
    }

    public Carrito checkout(String carritoId) {
        return checkout(carritoId, null);
    }

    public Carrito asociarUsuario(String carritoId, String usuarioId) {

        Carrito carrito = obtener(carritoId);

        carrito.setUsuarioId(usuarioId);

        return repo.save(carrito);
    }

    public Carrito asociarOFusionar(String carritoAnonimoId, String usuarioId) {

        Carrito carritoAnonimo = obtener(carritoAnonimoId);

        if (carritoAnonimo.getUsuarioId() != null && carritoAnonimo.getUsuarioId().equals(usuarioId)) {
            return carritoAnonimo;
        }

        Optional<Carrito> carritoUsuarioOpt = repo.findByUsuarioIdAndEstado(usuarioId, CarritoEstado.ABIERTO);

        if (carritoUsuarioOpt.isPresent() && carritoUsuarioOpt.get().getId().equals(carritoAnonimoId)) {
            return carritoAnonimo;
        }

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