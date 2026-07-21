package com.unrn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unrn.DTO.ActualizarCantidadDTO;
import com.unrn.DTO.AgregarItemDTO;
import com.unrn.DTO.EliminarItemDTO;
import com.unrn.model.Carrito;
import com.unrn.services.CarritoServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarritoControlador.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CarritoControllerTest.SecurityConfig.class)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarritoServicio servicio;

    @TestConfiguration
    static class SecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

            return http.build();
        }
    }

    private Carrito crearCarrito() {

        Carrito carrito = new Carrito();
        carrito.setId("carrito-1");
        carrito.setUsuarioId("evangelina");

        return carrito;
    }

    @Test
    @DisplayName("Debe crear un carrito para un usuario")
    void deberiaCrearCarritoUsuario() throws Exception {

        Carrito carrito = crearCarrito();

        when(servicio.crear("evangelina"))
                .thenReturn(carrito);

        mockMvc.perform(post("/carritos/evangelina"))
                .andExpect(status().isOk());

        verify(servicio).crear("evangelina");
    }

    @Test
    @DisplayName("Debe crear un carrito anónimo")
    void deberiaCrearCarritoAnonimo() throws Exception {

        Carrito carrito = crearCarrito();

        when(servicio.crear(null))
                .thenReturn(carrito);

        mockMvc.perform(post("/carritos"))
                .andExpect(status().isOk());

        verify(servicio).crear(null);
    }

    @Test
    @DisplayName("Debe obtener un carrito por usuario")
    void deberiaObtenerCarritoUsuario() throws Exception {

        Carrito carrito = crearCarrito();

        when(servicio.obtenerCarritoAbierto("evangelina"))
                .thenReturn(carrito);

        mockMvc.perform(get("/carritos/usuario/evangelina"))
                .andExpect(status().isOk());

        verify(servicio).obtenerCarritoAbierto("evangelina");
    }

    @Test
    @DisplayName("Debe obtener un carrito por id")
    void deberiaObtenerCarritoPorId() throws Exception {

        Carrito carrito = crearCarrito();

        when(servicio.obtener("carrito-1"))
                .thenReturn(carrito);

        mockMvc.perform(get("/carritos/carrito-1"))
                .andExpect(status().isOk());

        verify(servicio).obtener("carrito-1");
    }

    @Test
    @DisplayName("Debe agregar un item")
    void deberiaAgregarItem() throws Exception {

        AgregarItemDTO dto = new AgregarItemDTO(1, 2);

        Carrito carrito = crearCarrito();

        when(servicio.agregarItem(anyString(), anyInt(), anyInt()))
                .thenReturn(carrito);

        mockMvc.perform(post("/carritos/agregar-item/carrito-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(servicio)
                .agregarItem("carrito-1", 1, 2);
    }

    @Test
    @DisplayName("Debe actualizar la cantidad")
    void deberiaActualizarCantidad() throws Exception {

        ActualizarCantidadDTO dto = new ActualizarCantidadDTO(1, 5);

        Carrito carrito = crearCarrito();

        when(servicio.actualizarCantidad(anyString(), anyInt(), anyInt()))
                .thenReturn(carrito);

        mockMvc.perform(put("/carritos/actualizar-cantidad/carrito-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(servicio)
                .actualizarCantidad("carrito-1", 1, 5);
    }

    @Test
    @DisplayName("Debe eliminar un item")
    void deberiaEliminarItem() throws Exception {

        EliminarItemDTO dto = new EliminarItemDTO(1);

        Carrito carrito = crearCarrito();

        when(servicio.eliminarItem(anyString(), anyInt()))
                .thenReturn(carrito);

        mockMvc.perform(delete("/carritos/eliminar-item/carrito-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(servicio)
                .eliminarItem("carrito-1", 1);
    }

    @Test
    @DisplayName("Debe realizar checkout")
    void deberiaRealizarCheckout() throws Exception {

        Carrito carrito = crearCarrito();

        when(servicio.checkout("carrito-1"))
                .thenReturn(carrito);

        mockMvc.perform(post("/carritos/checkout/carrito-1"))
                .andExpect(status().isOk());

        verify(servicio)
                .checkout("carrito-1");
    }

    @Test
    @DisplayName("Debe asociar o fusionar un carrito")
    void deberiaAsociarOFusionar() throws Exception {

        Carrito carrito = crearCarrito();

        when(servicio.asociarOFusionar("carrito-1", "evangelina"))
                .thenReturn(carrito);

        mockMvc.perform(
                put("/carritos/carrito-1/fusionar/evangelina"))
                .andExpect(status().isOk());

        verify(servicio)
                .asociarOFusionar("carrito-1", "evangelina");
    }

    @Test
    @DisplayName("No debe agregar un item cuando el body es inválido")
    void noDeberiaAgregarItemConBodyInvalido() throws Exception {

        AgregarItemDTO dto = new AgregarItemDTO(null, 2);

        mockMvc.perform(post("/carritos/agregar-item/carrito-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(servicio);
    }

    @Test
    @DisplayName("No debe actualizar cantidad cuando es menor o igual a cero")
    void noDeberiaActualizarCantidadInvalida() throws Exception {

        ActualizarCantidadDTO dto = new ActualizarCantidadDTO(1, 0);

        mockMvc.perform(put("/carritos/actualizar-cantidad/carrito-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(servicio);
    }

    @Test
    @DisplayName("No debe eliminar un item cuando peliculaId es null")
    void noDeberiaEliminarItemConBodyInvalido() throws Exception {

        EliminarItemDTO dto = new EliminarItemDTO(null);

        mockMvc.perform(delete("/carritos/eliminar-item/carrito-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(servicio);
    }

}