package com.unrn.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.*;

class CompraEventPublisherTest {

    private RabbitTemplate rabbitTemplate;

    private CompraEventPublisher publisher;

    @BeforeEach
    void setUp() {

        rabbitTemplate = mock(RabbitTemplate.class);

        publisher = new CompraEventPublisher(
                rabbitTemplate,
                "exchange-test"
        );
    }

    @Test
    @DisplayName("Debe enviar un evento a RabbitMQ")
    void deberiaEnviarEvento() {

        Object evento = new Object();

        publisher.enviarEvento(evento);

        verify(rabbitTemplate).convertAndSend(
                "exchange-test",
                "compra.event",
                evento
        );
    }

    @Test
    @DisplayName("Debe enviar cualquier tipo de objeto como evento")
    void deberiaEnviarCualquierObjeto() {

        String evento = "Compra realizada";

        publisher.enviarEvento(evento);

        verify(rabbitTemplate).convertAndSend(
                "exchange-test",
                "compra.event",
                evento
        );
    }

}
