package com.unrn.services.externo;

import com.unrn.model.PeliculaSnapshot;
import com.unrn.repository.PeliculaSnapshotRepository;
import com.unrn.services.port.ClientePeliculasPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ClientePeliculas implements ClientePeliculasPort {

    private final PeliculaSnapshotRepository peliculaSnapshotRepository;
    private final RestTemplate restTemplate;
    private final String peliculasBaseUrl;

    @Autowired
    public ClientePeliculas(
            PeliculaSnapshotRepository peliculaSnapshotRepository,
            @Value("${peliculas.base.url:http://peliculas-app:8087}") String peliculasBaseUrl) {
        this.peliculaSnapshotRepository = peliculaSnapshotRepository;
        this.restTemplate = new RestTemplate();
        this.peliculasBaseUrl = peliculasBaseUrl;
    }

    @Override
    public PeliculaRemota obtenerPorId(Integer id) {
        return peliculaSnapshotRepository.findById(id)
                .filter(s -> s.getTitulo() != null && s.getPrecio() != null && s.getPrecio().compareTo(BigDecimal.ZERO) > 0)
                .map(snapshot -> new PeliculaRemota(
                        snapshot.getPeliculaId(),
                        snapshot.getTitulo(),
                        snapshot.getPrecio(),
                        snapshot.getImagenAmpliada(),
                        snapshot.getStock()
                ))
                .orElseGet(() -> consultarYGuardarRemota(id));
    }

    private PeliculaRemota consultarYGuardarRemota(Integer id) {
        try {
            String url = peliculasBaseUrl + "/peliculas/" + id;
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            if (resp != null && resp.containsKey("peliculaId")) {
                Integer peliculaId = ((Number) resp.get("peliculaId")).intValue();
                String titulo = (String) resp.get("titulo");
                BigDecimal precio = BigDecimal.valueOf(((Number) resp.get("precio")).doubleValue());
                String imagenAmpliada = (String) resp.get("imagenAmpliada");
                Integer stock = resp.get("stock") != null ? ((Number) resp.get("stock")).intValue() : 0;

                PeliculaSnapshot snapshot = peliculaSnapshotRepository.findById(id)
                        .orElseGet(PeliculaSnapshot::new);
                snapshot.setPeliculaId(peliculaId);
                snapshot.setTitulo(titulo);
                snapshot.setPrecio(precio);
                snapshot.setImagenAmpliada(imagenAmpliada);
                snapshot.setStock(stock);
                snapshot.setLastUpdate(LocalDateTime.now());

                peliculaSnapshotRepository.save(snapshot);

                return new PeliculaRemota(peliculaId, titulo, precio, imagenAmpliada, stock);
            }
        } catch (Exception e) {
            System.err.println("Fallback: Error consultando película remota ID " + id + ": " + e.getMessage());
        }

        return new PeliculaRemota(
                id,
                "Película " + id,
                BigDecimal.ZERO,
                null,
                0
        );
    }

    @Override
    public void descontarStock(DescuentoStockRequest request) {
        if (request == null || request.peliculas() == null || request.peliculas().isEmpty()) {
            return;
        }

        String url = peliculasBaseUrl + "/peliculas/descontar-stock";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DescuentoStockRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            String errorMsg = "No hay stock suficiente para la compra.";
            if (responseBody != null && !responseBody.isBlank()) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(No hay stock suficiente \"[^\"]+\" para la compra\\.)").matcher(responseBody);
                if (matcher.find()) {
                    errorMsg = matcher.group(1);
                } else if (responseBody.contains("No hay stock suficiente") || responseBody.contains("Stock insuficiente")) {
                    errorMsg = responseBody;
                }
            }
            throw new ResponseStatusException(e.getStatusCode(), errorMsg, e);
        } catch (Exception e) {
            System.err.println("Error llamando a servicio de películas para descontar stock: " + e.getMessage());
            // Si el servicio no está accesible por red (p. ej. en tests aislados o sin servicio de películas), no romper el checkout
        }
    }
}
