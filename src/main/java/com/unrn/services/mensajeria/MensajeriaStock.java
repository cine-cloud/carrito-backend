package com.unrn.services.mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unrn.model.Carrito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.unrn.services.mensajeria.MensajeriaConfiguracion.EXC_INVENTARIO_CMD;

@Service
public class MensajeriaStock {

  private final RabbitTemplate template;
  private final ObjectMapper mapper;

  public MensajeriaStock(RabbitTemplate template, ObjectMapper mapper) {
    this.template = template; this.mapper = mapper;
  }

  public void solicitarReserva(Carrito carrito) {
    var cmd = new ComandoReservarStock(
      carrito.getId(),
      carrito.getItems().stream()
        .map(i -> new ComandoReservarStock.Item(i.getPeliculaId(), i.getCantidad()))
        .toList()
    );
    enviar(EXC_INVENTARIO_CMD, "stock.reservar", cmd);
  }

  private void enviar(String exchange, String rk, Object payload) {
    try {
      String json = mapper.writeValueAsString(payload);
      Message msg = MessageBuilder.withBody(json.getBytes(StandardCharsets.UTF_8))
        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
        .setHeader("x-tipo-mensaje", rk)
        .setHeader("x-correlation-id", UUID.randomUUID().toString())
        .build();
      template.convertAndSend(exchange, rk, msg);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}

