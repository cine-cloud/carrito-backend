package com.unrn.event;

import com.unrn.event.dto.PrecioActualizadoEventDTO;
import com.unrn.model.Carrito;
import com.unrn.model.CarritoEstado;
import com.unrn.repository.CarritoRepositorio;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrecioPeliculaListener {

    private final CarritoRepositorio carritoRepositorio;

    public PrecioPeliculaListener(CarritoRepositorio carritoRepositorio) {
        this.carritoRepositorio = carritoRepositorio;
    }

    @RabbitListener(queues = "carrito_precio_queue")
    public void recibirEvento(PrecioActualizadoEventDTO evento) {

        var carritos =
            carritoRepositorio.findByEstado(
                    CarritoEstado.ABIERTO);

        for (Carrito carrito : carritos) {

            carrito.getItems().stream()
                .filter(item ->
                        item.getPeliculaId()
                                .equals(evento.getPeliculaId()))
                .forEach(item ->
                        item.setPrecioUnitario(
                                evento.getNuevoPrecio()));

            carrito.recalcular();

            carritoRepositorio.save(carrito);
        }
    }
}
