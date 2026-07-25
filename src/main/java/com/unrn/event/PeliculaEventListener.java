package com.unrn.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unrn.event.dto.Event;
import com.unrn.event.dto.EventType;
import com.unrn.event.dto.PeliculaSimplificadaDTO;
import com.unrn.model.Carrito;
import com.unrn.model.CarritoEstado;
import com.unrn.repository.CarritoRepositorio;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PeliculaEventListener {

    private final CarritoRepositorio carritoRepositorio;
    private final ObjectMapper objectMapper;

    public PeliculaEventListener(CarritoRepositorio carritoRepositorio, ObjectMapper objectMapper) {
        this.carritoRepositorio = carritoRepositorio;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${rabbitmq.event.consumer.queue.name}")
    public void recibirEventoPelicula(Event<Integer, Object> evento) {
        if (evento == null || evento.getEventType() != EventType.UPDATE || evento.getData() == null) {
            return;
        }

        try {
            PeliculaSimplificadaDTO pelicula = objectMapper.convertValue(evento.getData(), PeliculaSimplificadaDTO.class);
            if (pelicula == null || pelicula.getPeliculaId() == null) {
                return;
            }

            List<Carrito> carritosAbiertos = carritoRepositorio.findByEstado(CarritoEstado.ABIERTO);

            for (Carrito carrito : carritosAbiertos) {
                boolean modificado = false;
                for (var item : carrito.getItems()) {
                    if (item.getPeliculaId().equals(pelicula.getPeliculaId())) {
                        if (pelicula.getPrecio() != null) {
                            item.setPrecioUnitario(pelicula.getPrecio());
                        }
                        if (pelicula.getTitulo() != null) {
                            item.setTituloSnapshot(pelicula.getTitulo());
                        }
                        if (pelicula.getImagenAmpliada() != null) {
                            item.setImagenUrl(pelicula.getImagenAmpliada());
                        }
                        modificado = true;
                    }
                }

                if (modificado) {
                    carrito.recalcular();
                    carritoRepositorio.save(carrito);
                    System.out.println("Carrito ID " + carrito.getId() + " actualizado con los nuevos datos de la película ID " + pelicula.getPeliculaId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando evento de película en carrito-backend: " + e.getMessage());
        }
    }
}
