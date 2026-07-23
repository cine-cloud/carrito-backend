package com.unrn.services.Externo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClientePeliculasTest {

    @Test
    @DisplayName("PeliculaRemota record getters y constructor")
    void testPeliculaRemota() {
        ClientePeliculas.PeliculaRemota p = new ClientePeliculas.PeliculaRemota(1, "Test", new BigDecimal("50.00"));
        assertEquals(1, p.peliculaId());
        assertEquals("Test", p.titulo());
        assertEquals(new BigDecimal("50.00"), p.precio());
    }

    @Test
    @DisplayName("obtenerPorId exitoso")
    void testObtenerPorIdExitoso() {
        RestTemplate mockRest = mock(RestTemplate.class);
        ClientePeliculas.PeliculaRemota mockPeli = new ClientePeliculas.PeliculaRemota(10, "Peli 10", new BigDecimal("99.99"));

        when(mockRest.getForEntity(anyString(), eq(ClientePeliculas.PeliculaRemota.class), eq(10)))
                .thenReturn(new ResponseEntity<>(mockPeli, HttpStatus.OK));

        ClientePeliculas cliente = new ClientePeliculas(mockRest, "http://localhost:8080");
        ClientePeliculas.PeliculaRemota result = cliente.obtenerPorId(10);

        assertNotNull(result);
        assertEquals(10, result.peliculaId());
        assertEquals("Peli 10", result.titulo());
    }

    @Test
    @DisplayName("obtenerPorId respuesta fallida lanza IllegalStateException")
    void testObtenerPorIdFallidoLanzaExcepcion() {
        RestTemplate mockRest = mock(RestTemplate.class);

        when(mockRest.getForEntity(anyString(), eq(ClientePeliculas.PeliculaRemota.class), eq(99)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        ClientePeliculas cliente = new ClientePeliculas(mockRest, "http://localhost:8080");

        assertThrows(IllegalStateException.class, () -> cliente.obtenerPorId(99));
    }
}
