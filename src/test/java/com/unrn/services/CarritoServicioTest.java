package com.unrn.services;

import com.unrn.model.*;
import com.unrn.repository.CarritoRepositorio;
import com.unrn.services.Externo.ClientePeliculas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServicioTest {

    @Mock
    private CarritoRepositorio repo;

    @Mock
    private ClientePeliculas clientePeliculas;

    @InjectMocks
    private CarritoServicio servicio;

    private Carrito carrito;

    @BeforeEach
    void setUp() {
        carrito = new Carrito();
        carrito.setId("c1");
        carrito.setUsuarioId("u1");
        carrito.setEstado(CarritoEstado.ABIERTO);
    }

    @Test
    @DisplayName("Crear carrito de compras exitosamente")
    void testCrear() {
        when(repo.save(any(Carrito.class))).thenReturn(carrito);

        Carrito res = servicio.crear("u1");
        assertNotNull(res);
        assertEquals("u1", res.getUsuarioId());
    }

    @Test
    @DisplayName("Agregar item al carrito exitosamente")
    void testAgregarItem() {
        when(repo.findById("c1")).thenReturn(Optional.of(carrito));
        when(clientePeliculas.obtenerPorId(10))
                .thenReturn(new ClientePeliculas.PeliculaRemota(10, "Pelicula Test", new BigDecimal("100.00")));
        when(repo.save(any(Carrito.class))).thenReturn(carrito);

        Carrito res = servicio.agregarItem("c1", 10, 2);
        assertNotNull(res);
        verify(repo, times(1)).save(carrito);
    }

    @Test
    @DisplayName("Actualizar cantidad de item")
    void testActualizarCantidad() {
        when(repo.findById("c1")).thenReturn(Optional.of(carrito));
        when(repo.save(any(Carrito.class))).thenReturn(carrito);

        Carrito res = servicio.actualizarCantidad("c1", 10, 5);
        assertNotNull(res);
    }

    @Test
    @DisplayName("Eliminar item del carrito")
    void testEliminarItem() {
        when(repo.findById("c1")).thenReturn(Optional.of(carrito));
        when(repo.save(any(Carrito.class))).thenReturn(carrito);

        Carrito res = servicio.eliminarItem("c1", 10);
        assertNotNull(res);
    }

    @Test
    @DisplayName("Checkout carrito exitoso")
    void testCheckoutExitoso() {
        CarritoItem item = new CarritoItem();
        item.setPeliculaId(10);
        item.setCantidad(1);
        item.setPrecioUnitario(new BigDecimal("100.00"));
        carrito.agregarItem(item);

        when(repo.findById("c1")).thenReturn(Optional.of(carrito));
        when(repo.save(any(Carrito.class))).thenReturn(carrito);

        Carrito res = servicio.checkout("c1");
        assertEquals(CarritoEstado.CONFIRMADO, res.getEstado());
    }

    @Test
    @DisplayName("Checkout carrito vacío lanza IllegalStateException")
    void testCheckoutVacioLanzaExcepcion() {
        when(repo.findById("c1")).thenReturn(Optional.of(carrito));

        assertThrows(IllegalStateException.class, () -> servicio.checkout("c1"));
    }

    @Test
    @DisplayName("Modificar carrito confirmado lanza IllegalStateException")
    void testModificarCarritoConfirmadoLanzaExcepcion() {
        carrito.setEstado(CarritoEstado.CONFIRMADO);
        when(repo.findById("c1")).thenReturn(Optional.of(carrito));

        assertThrows(IllegalStateException.class, () -> servicio.actualizarCantidad("c1", 10, 2));
    }
}
