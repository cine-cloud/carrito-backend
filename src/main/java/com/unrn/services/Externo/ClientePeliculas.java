package com.unrn.services.Externo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ClientePeliculas {

    private final RestTemplate rest;
    private final String baseUrl;

    public ClientePeliculas(
            RestTemplate rest,
            @Value("${peliculas.base-url}") String baseUrl) {

        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    public PeliculaRemota obtenerPorId(Integer id) {

        var resp = rest.getForEntity(
                baseUrl + "/peliculas/{id}",
                PeliculaRemota.class,
                id);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException(
                    "No se pudo obtener Película " + id);
        }

        return resp.getBody();
    }

    public void descontarStock(DescuentoStockRequest request) {

        HttpEntity<DescuentoStockRequest> entity =
                new HttpEntity<>(request);

        rest.exchange(
                baseUrl + "/peliculas/descontar-stock",
                HttpMethod.PUT,
                entity,
                Void.class);
    }

    public record PeliculaRemota(
            Integer peliculaId,
            String titulo,
            BigDecimal precio,
            String imagenAmpliada
    ) {
    }

    public record DescuentoStockDTO(
            Integer peliculaId,
            Integer cantidad
    ) {
    }

    public record DescuentoStockRequest(
            List<DescuentoStockDTO> peliculas
    ) {
    }
}