package com.unrn.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CompraEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final String exchangeName;

    public CompraEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.event.exchange.name}") String exchangeName) {

        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }

   public void enviarEvento(Object evento) {

    System.out.println(
            "ENVIANDO EVENTO A RABBIT: " + evento
    );

    rabbitTemplate.convertAndSend(
            exchangeName,
            "compra.event",
            evento
    );

    System.out.println(
            "EVENTO ENVIADO"
    );
}
}