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

import java.util.Map;

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
    public void handleMovieEvent(Event<String, Map<String, Object>> event) {
        log.info("EVENTO RECIBIDO en Carrito-Backend: Tipo={} para Movie ID={}", event.getEventType(), event.getKey());

        switch (event.getEventType()) {
            case CREATE:
            case UPDATE:
                Pelicula pelicula = convertirMapAPelicula(event.getData());
                peliculaRepository.save(pelicula);
                log.info("Película {} guardada/actualizada en BD de carrito", event.getKey());
                break;
            case DELETE:
                peliculaRepository.deleteById(event.getKey());
                log.info("Película {} eliminada de BD de carrito", event.getKey());
                break;
            default:
                log.warn("Tipo de evento de película no manejado: {}", event.getEventType());
        }
    }

    private Pelicula convertirMapAPelicula(Map<String, Object> data) {
        Pelicula p = new Pelicula();
        p.setPeliculaId((String) data.get("peliculaId"));
        p.setTitulo((String) data.get("titulo"));
        p.setFechaSalida(java.time.LocalDate.parse(data.get("fechaSalida").toString()));
        p.setPrecio(new java.math.BigDecimal(data.get("precio").toString()));
        p.setCondicion((String) data.get("condicion"));
        p.setFormato((String) data.get("formato"));
        p.setSinopsis((String) data.get("sinopsis"));
        p.setImagenAmpliada((String) data.get("imagenAmpliada"));
        if (data.get("lastUpdate") != null) {
            p.setLastUpdate(java.time.LocalDateTime.parse(data.get("lastUpdate").toString()));
        } else {
            p.setLastUpdate(java.time.LocalDateTime.now());
        }
        return p;
    }

    // Recovery method: Executed if the message fails after 3 retry attempts
    @Recover
    public void recover(Exception e, Event<String, Map<String, Object>> event) {
        log.error(
                "FALLO PERMANENTE al procesar evento de Pelicula ID: {}. El mensaje debe ser revisado manualmente o enviado a DLQ.",
                event.getKey(), e);
    }
}