package com.unrn.model;

import com.unrn.DTO.ActualizarCantidadDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CarritoTest {

    @Test
    @DisplayName("Probando metodos del modelo Carrito y CarritoItem")
    void testCarritoModel() {
        Carrito c = new Carrito();
        c.setId("c1");
        c.setUsuarioId("u1");
        c.setEstado(CarritoEstado.ABIERTO);

        CarritoItem item = new CarritoItem();
        item.setId("item1");
        item.setPeliculaId(10);
        item.setTituloSnapshot("Peli 10");
        item.setPrecioUnitario(new BigDecimal("100.00"));
        item.setCantidad(2);

        c.agregarItem(item);
        assertEquals(1, c.getItems().size());
        assertEquals(new BigDecimal("200.00"), c.getTotal());

        c.actualizarCantidad(10, 5);
        assertEquals(5, item.getCantidad());
        assertEquals(new BigDecimal("500.00"), c.getTotal());

        c.eliminarItem(10);
        assertTrue(c.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, c.getTotal());
    }

    @Test
    @DisplayName("Probando DTO ActualizarCantidadDTO")
    void testActualizarCantidadDTO() {
        ActualizarCantidadDTO dto = new ActualizarCantidadDTO(1, 5);
        assertEquals(5, dto.cantidad());
    }
}
