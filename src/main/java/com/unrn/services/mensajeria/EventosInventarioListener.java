package com.unrn.services.mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unrn.services.CarritoServicio;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static com.unrn.services.mensajeria.MensajeriaConfiguracion.Q_CARRITO_EVENTOS;

@Component
public class EventosInventarioListener {
  private final ObjectMapper mapper;
  private final CarritoServicio servicio;

  public EventosInventarioListener(ObjectMapper mapper, CarritoServicio servicio) {
    this.mapper = mapper; this.servicio = servicio;
  }

  @RabbitListener(queues = Q_CARRITO_EVENTOS, concurrency = "1-4")
  public void onEvento(Message msg) throws Exception {
    String tipo = (String) msg.getMessageProperties().getHeaders().get("x-tipo-mensaje");
    String body = new String(msg.getBody(), StandardCharsets.UTF_8);

    switch (tipo) {
      case "stock.reservado" ->
        servicio.onStockReservado(mapper.readValue(body, EventoStockReservado.class).carritoId());
      case "stock.rechazado" -> {
        var evt = mapper.readValue(body, EventoStockRechazado.class);
        servicio.onStockRechazado(evt.carritoId(), evt.motivo());
      }
      default -> { /* log warn */ }
    }
  }
}
