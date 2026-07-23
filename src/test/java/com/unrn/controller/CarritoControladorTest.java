package com.unrn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unrn.controller.DTO.AgregarItemDTO;
import com.unrn.model.Carrito;
import com.unrn.model.CarritoEstado;
import com.unrn.services.CarritoServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoControlador.class)
class CarritoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarritoServicio servicio;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /carritos/{usuarioId} - Crear carrito (200 OK)")
    void testCrearCarrito() throws Exception {
        Carrito c = new Carrito();
        c.setId("c1");
        c.setUsuarioId("u1");
        c.setEstado(CarritoEstado.ABIERTO);

        when(servicio.crear("u1")).thenReturn(c);

        mockMvc.perform(post("/carritos/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c1"))
                .andExpect(jsonPath("$.usuarioId").value("u1"));
    }

    @Test
    @DisplayName("GET /carritos/{idCarrito} - Obtener carrito (200 OK)")
    void testObtenerCarrito() throws Exception {
        Carrito c = new Carrito();
        c.setId("c1");
        c.setUsuarioId("u1");

        when(servicio.obtener("c1")).thenReturn(c);

        mockMvc.perform(get("/carritos/c1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c1"));
    }

    @Test
    @DisplayName("POST /carritos/agregar-item/{idCarrito} - Agregar item (200 OK)")
    void testAgregarItemCarrito() throws Exception {
        Carrito c = new Carrito();
        c.setId("c1");

        AgregarItemDTO dto = new AgregarItemDTO(10, 2);

        when(servicio.agregarItem(anyString(), anyInt(), anyInt())).thenReturn(c);

        mockMvc.perform(post("/carritos/agregar-item/c1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c1"));
    }
}
