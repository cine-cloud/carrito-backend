package com.unrn.services.mensajeria;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MensajeriaConfiguracion {
  public static final String EXC_INVENTARIO_CMD = "inventario.comandos";
  public static final String EXC_INVENTARIO_EVT = "inventario.eventos";
  public static final String Q_CARRITO_EVENTOS = "carrito-eventos-q";
  public static final String DLX = "carrito-dlx";
  public static final String DLQ = "carrito-dlq";

  @Bean TopicExchange inventarioComandos(){ return new TopicExchange(EXC_INVENTARIO_CMD, true,false); }
  @Bean TopicExchange inventarioEventos(){ return new TopicExchange(EXC_INVENTARIO_EVT, true,false); }

  @Bean DirectExchange dlx(){ return new DirectExchange(DLX); }

  @Bean Queue carritoEventosQ(){
    return QueueBuilder.durable(Q_CARRITO_EVENTOS)
      .withArgument("x-dead-letter-exchange", DLX)
      .withArgument("x-dead-letter-routing-key", DLQ)
      .build();
  }
  @Bean Queue dlq(){ return QueueBuilder.durable(DLQ).build(); }

  @Bean Binding bindEventos(){
    return BindingBuilder.bind(carritoEventosQ()).to(inventarioEventos()).with("stock.*");
  }
  @Bean Binding bindDlq(){ return BindingBuilder.bind(dlq()).to(dlx()).with(DLQ); }
}

