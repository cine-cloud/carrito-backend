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

    @Value("${rabbitmq.event.exchange.name}")
    private String eventExchange;

    @Value("${rabbitmq.event.consumer.queue.name}")
    private String queueName;

    @Value("${rabbitmq.event.movie.routing.key}")
    private String routingKey;

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
    public TopicExchange eventExchange() {
        return new TopicExchange(eventExchange);
    }

    @Bean
    public Queue consumerQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding binding(Queue consumerQueue, TopicExchange eventExchange) {
        return BindingBuilder
                .bind(consumerQueue)
                .to(eventExchange)
                .with(routingKey); // 🔄 película.event
    }

    @Bean
    public Queue precioQueue() {
        return new Queue("carrito_precio_queue", true);
    }

    @Bean
    public Binding precioBinding(
        Queue precioQueue,
        TopicExchange exchange) {

        return BindingBuilder
            .bind(precioQueue)
            .to(exchange)
            .with("pelicula.precio.actualizado");
    }

}