package com.unrn.carritos.event;

import com.unrn.carritos.domain.Pelicula;
import com.unrn.carritos.event.dto.Event;
import com.unrn.carritos.model.Carrito;
import com.unrn.carritos.repository.CarritoItemRepository;
import com.unrn.carritos.repository.CarritoRepository;
import com.unrn.carritos.repository.PeliculaRepository;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.unrn.carritos.model.CarritoItem;
import com.unrn.carritos.model.CarritoEstado;

@Service
public class PeliculaEventListener {

    private final PeliculaRepository peliculaRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final CarritoRepository carritoRepository;

    @Autowired
    public PeliculaEventListener(
            PeliculaRepository peliculaRepository,
            CarritoItemRepository carritoItemRepository,
            CarritoRepository carritoRepository) {
        this.peliculaRepository = peliculaRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.carritoRepository = carritoRepository;
    }

    @RabbitListener(queues = "${rabbitmq.event.consumer.queue.name}")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    @Transactional
    public void handleMovieEvent(Event<Integer, Map<String, Object>> event) {

        if (event.getKey() == null) {
            throw new IllegalArgumentException("El key (peliculaId) no puede ser null");
        }

        switch (event.getEventType()) {

            case CREATE:
                Pelicula peliculaCreate = convertirMapAPelicula(event.getData(), event.getKey());
                peliculaRepository.save(peliculaCreate);
                break;

            case UPDATE:
                Pelicula pelicula = convertirMapAPelicula(event.getData(), event.getKey());
                peliculaRepository.save(pelicula);
                
                // Actualizar snapshots de items del carrito que tengan esta película
                Integer peliculaId = event.getKey();
                List<CarritoItem> items = carritoItemRepository.findByPeliculaId(peliculaId);
                
                if (!items.isEmpty()) {
                    // Filtrar items que pertenecen a carritos ABIERTOS (solo estos se actualizan)
                    List<CarritoItem> itemsAbiertos = new ArrayList<>();
                    Set<String> carritoIdsAbiertos = new HashSet<>();
                    
                    for (CarritoItem item : items) {
                        // Forzar carga de la relación lazy
                        Carrito carrito = item.getCarrito();
                        if (carrito != null && carrito.getEstado() == CarritoEstado.ABIERTO) {
                            itemsAbiertos.add(item);
                            carritoIdsAbiertos.add(carrito.getId());
                        }
                    }
                    
                    // Actualizar snapshots solo de items en carritos ABIERTOS
                    if (!itemsAbiertos.isEmpty()) {
                        for (CarritoItem item : itemsAbiertos) {
                            item.setTituloSnapshot(pelicula.getTitulo());
                            item.setPrecioUnitario(pelicula.getPrecio());
                            item.setSinopsisSnapshot(pelicula.getSinopsis());
                            item.setImagenAmpliadaSnapshot(pelicula.getImagenAmpliada());
                            item.setCondicionSnapshot(pelicula.getCondicion());
                            item.setFormatoSnapshot(pelicula.getFormato());
                        }
                        carritoItemRepository.saveAll(itemsAbiertos);
                        
                        // Recalcular totales solo de los carritos ABIERTOS afectados
                        for (String carritoId : carritoIdsAbiertos) {
                            Carrito carrito = carritoRepository.findById(carritoId).orElse(null);
                            if (carrito != null) {
                                carrito.recalcular();
                                carritoRepository.save(carrito);
                            }
                        }
                    }
                }
                break;

            case DELETE:
                peliculaRepository.deleteById(event.getKey());
                break;

            default:
                // Tipo de evento no manejado
                break;
        }
    }

    private Pelicula convertirMapAPelicula(Map<String, Object> data, Integer peliculaId) {

        if (data == null) {
            throw new IllegalArgumentException("El objeto data del evento no puede ser null");
        }

        Pelicula p = new Pelicula();
        p.setPeliculaId(peliculaId);

        // --- CAMPOS OBLIGATORIOS ---
        p.setTitulo(obtenerCampoObligatorio(data, "titulo"));
        p.setCondicion(obtenerCampoObligatorio(data, "condicion"));
        p.setFormato(obtenerCampoObligatorio(data, "formato"));

        // --- CAMPOS OPCIONALES ---
        if (data.get("sinopsis") != null)
            p.setSinopsis(data.get("sinopsis").toString());

        if (data.get("imagenAmpliada") != null)
            p.setImagenAmpliada(data.get("imagenAmpliada").toString());

        // --- FECHA ---
        Object fechaObj = data.get("fechaSalida");
        if (fechaObj == null) {
            throw new IllegalArgumentException("El campo 'fechaSalida' es requerido");
        }
        p.setFechaSalida(parsearFecha(fechaObj));

        // --- PRECIO ---
        String precioStr = obtenerCampoObligatorio(data, "precio");
        try {
            p.setPrecio(new BigDecimal(precioStr));
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de precio inválido: " + precioStr);
        }

        // --- LAST UPDATE ---
        Object lastUpdateObj = data.get("lastUpdate");
        if (lastUpdateObj != null) {
            try {
                p.setLastUpdate(parsearFechaHora(lastUpdateObj));
            } catch (Exception e) {
                p.setLastUpdate(LocalDateTime.now());
            }
        } else {
            p.setLastUpdate(LocalDateTime.now());
        }

        return p;
    }


    private String obtenerCampoObligatorio(Map<String, Object> data, String campo) {
        if (data.get(campo) == null) {
            throw new IllegalArgumentException("El campo '" + campo + "' es requerido");
        }
        return data.get(campo).toString();
    }

    private LocalDate parsearFecha(Object fechaObj) {
        if (fechaObj instanceof List) {
            // Formato array: [año, mes, día]
            List<?> fechaArray = (List<?>) fechaObj;
            if (fechaArray.size() >= 3) {
                int año = ((Number) fechaArray.get(0)).intValue();
                int mes = ((Number) fechaArray.get(1)).intValue();
                int dia = ((Number) fechaArray.get(2)).intValue();
                return LocalDate.of(año, mes, dia);
            }
            throw new IllegalArgumentException("Formato de fecha array inválido: " + fechaObj);
        } else if (fechaObj instanceof String) {
            // Formato string: "YYYY-MM-DD"
            return LocalDate.parse((String) fechaObj);
        } else {
            throw new IllegalArgumentException("Formato de fecha inválido: " + fechaObj);
        }
    }

    private LocalDateTime parsearFechaHora(Object fechaHoraObj) {
        if (fechaHoraObj instanceof List) {
            // Formato array: [año, mes, día, hora, minuto, segundo]
            List<?> fechaArray = (List<?>) fechaHoraObj;
            if (fechaArray.size() >= 3) {
                int año = ((Number) fechaArray.get(0)).intValue();
                int mes = ((Number) fechaArray.get(1)).intValue();
                int dia = ((Number) fechaArray.get(2)).intValue();
                int hora = fechaArray.size() > 3 ? ((Number) fechaArray.get(3)).intValue() : 0;
                int minuto = fechaArray.size() > 4 ? ((Number) fechaArray.get(4)).intValue() : 0;
                int segundo = fechaArray.size() > 5 ? ((Number) fechaArray.get(5)).intValue() : 0;
                return LocalDateTime.of(año, mes, dia, hora, minuto, segundo);
            }
            throw new IllegalArgumentException("Formato de fecha/hora array inválido: " + fechaHoraObj);
        } else if (fechaHoraObj instanceof String) {
            // Formato string: "YYYY-MM-DDTHH:mm:ss"
            return LocalDateTime.parse((String) fechaHoraObj);
        } else {
            throw new IllegalArgumentException("Formato de fecha/hora inválido: " + fechaHoraObj);
        }
    }

    @Recover
    public void recover(Exception e, Event<String, Map<String, Object>> event) {
        // Fallo permanente después de 3 intentos
    }
}
