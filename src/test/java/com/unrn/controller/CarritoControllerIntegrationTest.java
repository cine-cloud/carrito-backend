package com.unrn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unrn.CarritoApplication;
import com.unrn.event.CompraEventPublisher;
import com.unrn.model.Carrito;
import com.unrn.repository.CarritoRepositorio;
import com.unrn.services.Externo.ClientePeliculas;
import com.unrn.services.Externo.ClientePeliculas.PeliculaRemota;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest(classes = CarritoApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class CarritoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;    

    @Autowired
    private CarritoRepositorio repositorio;

    @MockBean
    private ClientePeliculas clientePeliculas;

    @MockBean
    private CompraEventPublisher compraEventPublisher;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void configurarMocks() {

        when(clientePeliculas.obtenerPorId(anyInt()))
                .thenReturn(
                        new PeliculaRemota(
                                1,
                                "Matrix",
                                new BigDecimal("1000"),
                                "imagen.jpg"));
    }

    @Test
    @DisplayName("Debe crear un carrito y persistirlo en la base")
    void crearCarrito() throws Exception {

        mockMvc.perform(
                post("/carritos/evangelina"))
                .andExpect(status().isOk());

        assertEquals(
                1,
                repositorio.count());

        Carrito carrito = repositorio.findAll().get(0);

        assertEquals(
                "evangelina",
                carrito.getUsuarioId());
    }

    @Test
    @DisplayName("Debe obtener un carrito persistido")
    void obenerCarrito() throws Exception {

        Carrito carrito = new Carrito();
        carrito.setUsuarioId("evangelina");

        carrito = repositorio.save(carrito);

        mockMvc.perform(
                get("/carritos/" + carrito.getId()))
                .andExpect(status().isOk());

        assertTrue(
                repositorio.findById(carrito.getId()).isPresent());
    }

    @Test
    @DisplayName("Debe crear un carrito anónimo")
    void crearCarritoAnonimo() throws Exception {

        mockMvc.perform(
                post("/carritos"))
                .andExpect(status().isOk());

        assertEquals(
                1,
                repositorio.count());

        Carrito carrito = repositorio.findAll().get(0);

        assertNull(carrito.getUsuarioId());
    }

    @Test
    @DisplayName("Debe agregar un item y persistirlo")
    void agregarItem() throws Exception {

        Carrito carrito = new Carrito();
        carrito.setUsuarioId("evangelina");

        carrito = repositorio.save(carrito);

        String json = """
                {
                  "peliculaId":1,
                  "cantidad":2
                }
                """;

        mockMvc.perform(
                post("/carritos/agregar-item/" + carrito.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Carrito actualizado = repositorio.findById(carrito.getId()).orElseThrow();

        assertEquals(
                1,
                actualizado.getItems().size());

        assertEquals(
                2,
                actualizado.getItems().get(0).getCantidad());

        assertEquals(
                0,
                new BigDecimal("2000")
                        .compareTo(actualizado.getTotal()));

        verify(clientePeliculas)
                .obtenerPorId(1);
    }

    @Test
    @DisplayName("Debe persistir correctamente los datos del item")
    void persistirDatosDelItem() throws Exception {

        Carrito carrito = new Carrito();

        carrito = repositorio.save(carrito);

        String json = """
                {
                  "peliculaId":1,
                  "cantidad":3
                }
                """;

        mockMvc.perform(
                post("/carritos/agregar-item/" + carrito.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Carrito actualizado = repositorio.findById(carrito.getId()).orElseThrow();

        assertEquals(
                "Matrix",
                actualizado.getItems().get(0).getTituloSnapshot());

        assertEquals(
                "imagen.jpg",
                actualizado.getItems().get(0).getImagenUrl());

        assertEquals(
                0,
                new BigDecimal("1000")
                        .compareTo(actualizado.getItems().get(0).getPrecioUnitario()));

        assertEquals(
                3,
                actualizado.getItems().get(0).getCantidad());
    }

}