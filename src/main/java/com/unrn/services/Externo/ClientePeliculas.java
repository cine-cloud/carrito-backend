package com.unrn.services.Externo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class ClientePeliculas {

  private final RestTemplate rest = new RestTemplate();
  private final String baseUrl;

  public ClientePeliculas(@Value("${peliculas.base-url}") String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public PeliculaRemota obtenerPorId(Integer id) {
    var resp = rest.getForEntity(baseUrl + "/peliculas/{id}", PeliculaRemota.class, id);
    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody()==null) {
      throw new IllegalStateException("No se pudo obtener Película " + id);
    }
    return resp.getBody();
  }

  public record PeliculaRemota(Integer peliculaId, String titulo, BigDecimal precio) {}
}