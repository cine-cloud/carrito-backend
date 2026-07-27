package com.unrn.services;

import com.unrn.CarritoApplication;
import com.unrn.event.CompraEventPublisher;
import com.unrn.model.Carrito;
import com.unrn.repository.CarritoRepositorio;
import com.unrn.services.externo.ClientePeliculas;
import com.unrn.services.port.ClientePeliculasPort.PeliculaRemota;
import com.unrn.services.port.ClientePeliculasPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest(classes = CarritoApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class CarritoServicioIntegrationTest {

    @Autowired
    private CarritoServicio servicio;

    @Autowired
    private CarritoRepositorio repositorio;

    @MockBean
    private ClientePeliculasPort clientePeliculas;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private CompraEventPublisher compraEventPublisher;

    @BeforeEach
    void configurarMocks() {

        when(clientePeliculas.obtenerPorId(anyInt()))
                .thenReturn(
                        new PeliculaRemota(
                                1,
                                "Matrix",
                                new BigDecimal("1000"),
                                "imagen.jpg",
                                10
                        )
                );
    }

    @Test
    @DisplayName("Debe crear un carrito")
    void crearCarrito() {

        Carrito carrito = servicio.crear("evangelina");

        assertNotNull(carrito.getId());

        assertEquals(
                "evangelina",
                carrito.getUsuarioId()
        );

        assertEquals(
                1,
                repositorio.count()
        );
    }

    @Test
    @DisplayName("Debe obtener un carrito abierto")
    void obtenerCarritoAbierto() {

        Carrito carrito = servicio.obtenerCarritoAbierto("evangelina");

        assertNotNull(carrito.getId());

        assertEquals(
                "evangelina",
                carrito.getUsuarioId()
        );

        assertEquals(
                1,
                repositorio.count()
        );
    }

    @Test
    @DisplayName("Debe reutilizar un carrito abierto existente")
    void reutilizarCarritoAbierto() {

        Carrito primero =
                servicio.obtenerCarritoAbierto("evangelina");

        Carrito segundo =
                servicio.obtenerCarritoAbierto("evangelina");

        assertEquals(
                primero.getId(),
                segundo.getId()
        );

        assertEquals(
                1,
                repositorio.count()
        );
    }

        @Test
    @DisplayName("Debe agregar un item al carrito")
    void agregarItem() {

        Carrito carrito = servicio.crear("evangelina");

        Carrito actualizado =
                servicio.agregarItem(carrito.getId(), 1, 2);

        assertEquals(1, actualizado.getItems().size());

        assertEquals(
                2,
                actualizado.getItems().get(0).getCantidad()
        );

        assertEquals(
                0,
                new BigDecimal("2000")
                        .compareTo(actualizado.getTotal())
        );

        verify(clientePeliculas).obtenerPorId(1);
    }

    @Test
    @DisplayName("Debe actualizar la cantidad de un item")
    void actualizarCantidad() {

        Carrito carrito = servicio.crear("evangelina");

        servicio.agregarItem(carrito.getId(), 1, 2);

        Carrito actualizado =
                servicio.actualizarCantidad(carrito.getId(), 1, 5);

        assertEquals(
                5,
                actualizado.getItems().get(0).getCantidad()
        );

        assertEquals(
                0,
                new BigDecimal("5000")
                        .compareTo(actualizado.getTotal())
        );
    }

    @Test
    @DisplayName("Debe eliminar un item")
    void eliminarItem() {

        Carrito carrito = servicio.crear("evangelina");

        servicio.agregarItem(carrito.getId(), 1, 2);

        Carrito actualizado =
                servicio.eliminarItem(carrito.getId(), 1);

        assertTrue(actualizado.getItems().isEmpty());

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(actualizado.getTotal())
        );
    }

    @Test
    @DisplayName("Debe realizar checkout")
    void checkout() {

        Carrito carrito = servicio.crear("evangelina");

        servicio.agregarItem(carrito.getId(), 1, 2);

        Carrito checkout =
                servicio.checkout(carrito.getId());

        assertEquals(
                com.unrn.model.CarritoEstado.CONFIRMADO,
                checkout.getEstado()
        );

        verify(compraEventPublisher)
                .enviarEvento(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el carrito está vacío")
    void checkoutCarritoVacio() {

        Carrito carrito = servicio.crear("evangelina");

        assertThrows(
                IllegalStateException.class,
                () -> servicio.checkout(carrito.getId())
        );

        verify(compraEventPublisher, never())
                .enviarEvento(any());
    }

    @Test
    @DisplayName("Debe asociar un carrito anónimo a un usuario")
    void asociarUsuario() {

        Carrito carrito = servicio.crear(null);

        Carrito asociado =
                servicio.asociarUsuario(
                        carrito.getId(),
                        "evangelina"
                );

        assertEquals(
                "evangelina",
                asociado.getUsuarioId()
        );
    }

    @Test
    @DisplayName("Debe fusionar un carrito anónimo con uno existente")
    void asociarOFusionar() {

        Carrito usuario =
                servicio.crear("evangelina");

        servicio.agregarItem(usuario.getId(), 1, 1);

        Carrito anonimo =
                servicio.crear(null);

        servicio.agregarItem(anonimo.getId(), 1, 2);

        Carrito fusionado =
                servicio.asociarOFusionar(
                        anonimo.getId(),
                        "evangelina"
                );

        assertEquals(
                3,
                fusionado.getItems().get(0).getCantidad()
        );

        assertEquals(
                1,
                repositorio.count()
        );
    }

}
