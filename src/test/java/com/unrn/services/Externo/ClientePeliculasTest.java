package com.unrn.services.Externo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ClientePeliculasTest {

    @Mock
    private RestTemplate restTemplate;

    private ClientePeliculas clientePeliculas;

    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        clientePeliculas = new ClientePeliculas(restTemplate, BASE_URL);
    }

    @Test
    void deberiaObtenerPeliculaPorId() {

        ClientePeliculas.PeliculaRemota pelicula =
                new ClientePeliculas.PeliculaRemota(
                        1,
                        "Matrix",
                        new BigDecimal("2500"),
                        "imagen.jpg");

        when(restTemplate.getForEntity(
                eq(BASE_URL + "/peliculas/{id}"),
                eq(ClientePeliculas.PeliculaRemota.class),
                eq(1)))
                .thenReturn(ResponseEntity.ok(pelicula));

        ClientePeliculas.PeliculaRemota resultado =
                clientePeliculas.obtenerPorId(1);

        assertEquals(1, resultado.peliculaId());
        assertEquals("Matrix", resultado.titulo());
        assertEquals(
                0,
                new BigDecimal("2500").compareTo(resultado.precio()));
        assertEquals("imagen.jpg", resultado.imagenAmpliada());

        verify(restTemplate).getForEntity(
                BASE_URL + "/peliculas/{id}",
                ClientePeliculas.PeliculaRemota.class,
                1);
    }

    @Test
    void deberiaLanzarExcepcionCuandoBodyEsNull() {

        ResponseEntity<ClientePeliculas.PeliculaRemota> response =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity(
                eq(BASE_URL + "/peliculas/{id}"),
                eq(ClientePeliculas.PeliculaRemota.class),
                eq(5)))
                .thenReturn(response);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> clientePeliculas.obtenerPorId(5));

        assertEquals(
                "No se pudo obtener Película 5",
                ex.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionCuandoRespuestaNoEsExitosa() {

        ResponseEntity<ClientePeliculas.PeliculaRemota> response =
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        when(restTemplate.getForEntity(
                eq(BASE_URL + "/peliculas/{id}"),
                eq(ClientePeliculas.PeliculaRemota.class),
                eq(9)))
                .thenReturn(response);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> clientePeliculas.obtenerPorId(9));

        assertEquals(
                "No se pudo obtener Película 9",
                ex.getMessage());
    }

    @Test
    void deberiaPropagarExcepcionDelRestTemplate() {

        when(restTemplate.getForEntity(
                eq(BASE_URL + "/peliculas/{id}"),
                eq(ClientePeliculas.PeliculaRemota.class),
                eq(3)))
                .thenThrow(new RestClientException("Error de conexión"));

        RestClientException ex = assertThrows(
                RestClientException.class,
                () -> clientePeliculas.obtenerPorId(3));

        assertEquals("Error de conexión", ex.getMessage());
    }
}
