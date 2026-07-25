package com.unrn.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.event.movie.exchange.name:pelicula_exchange}")
    private String movieExchangeName;

    @Value("${rabbitmq.event.movie.queue.name:carrito.pelicula.queue}")
    private String movieQueueName;

    @Value("${rabbitmq.event.movie.routing.key:pelicula.event}")
    private String movieRoutingKey;

    @Value("${rabbitmq.event.compra.exchange.name:compra.exchange}")
    private String compraExchangeName;

    @Bean
    public MessageConverter jsonMessageConverter(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
    
    @Bean
    public TopicExchange movieExchange() {
        return new TopicExchange(movieExchangeName);
    }

    @Bean
    public Queue movieQueue() {
        return new Queue(movieQueueName, true);
    }

    @Bean
    public Binding movieBinding(Queue movieQueue, TopicExchange movieExchange) {
        return BindingBuilder
                .bind(movieQueue)
                .to(movieExchange)
                .with(movieRoutingKey);
    }

    @Bean
    public TopicExchange compraExchange() {
        return new TopicExchange(compraExchangeName);
    }
}