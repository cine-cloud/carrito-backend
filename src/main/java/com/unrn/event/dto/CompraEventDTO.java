package com.unrn.event.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraEventDTO {

    private String usuarioId;
    private String fechaTransaccion;
    private BigDecimal subtotal;
    private BigDecimal descuentoMonto;
    private BigDecimal total;
    private List<ItemCompraEventDTO> items;

    // Campos adicionales para compatibilidad con notificaciones-backend
    private String idCompra;
    private LocalDateTime fecha;
    private String emailCliente;
    private String nombreCliente;
    private List<ProductoDTO> productos;

    @Data
    public static class ProductoDTO {
        private String idProducto;
        private String nombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}