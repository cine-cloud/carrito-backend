package com.unrn.carritos.event;

import com.unrn.carritos.domain.Pelicula;
import com.unrn.carritos.repository.PeliculaRepository;
import com.unrn.carritos.event.dto.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PeliculaEventListener {

    private final PeliculaRepository peliculaRepository;

    @Autowired
    public PeliculaEventListener(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    // Listener method consumes messages from the queue name defined in properties
    @RabbitListener(queues = { "${rabbitmq.event.consumer.queue.name}" })
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void handleMovieEvent(Event<String, Pelicula> event) {
        log.info("EVENTO RECIBIDO en Carrito-Backend: Tipo={} para Movie ID={}", event.getEventType(), event.getKey());

        switch (event.getEventType()) {
            case CREATE:
            case UPDATE:
                peliculaRepository.save(event.getData());
                break;
            case DELETE:
                peliculaRepository.deleteById(event.getKey());
                break;
            default:
                log.warn("Tipo de evento de película no manejado: {}", event.getEventType());
        }
    }

    // Recovery method: Executed if the message fails after 3 retry attempts
    @Recover
    public void recover(Exception e, Event<String, Pelicula> event) {
        log.error(
                "FALLO PERMANENTE al procesar evento de Pelicula ID: {}. El mensaje debe ser revisado manualmente o enviado a DLQ.",
                event.getKey(), e);
    }
}