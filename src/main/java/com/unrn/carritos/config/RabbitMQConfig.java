package com.unrn.carritos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Properties injected from application.properties
    @Value("${rabbitmq.event.exchange.name}")
    private String eventExchange;

    @Value("${rabbitmq.event.consumer.queue.name}")
    private String queueName;

    @Value("${rabbitmq.event.movie.routing.key}")
    private String routingKey;

    // Defines the JSON message converter for serialization
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Declares the Topic Exchange (shared with the producer)
    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(eventExchange);
    }

    // Declares the Queue unique to the Carrito microservice
    @Bean
    public Queue consumerQueue() {
        // durable = true ensures the queue survives a broker restart
        return new Queue(queueName, true);
    }

    // Creates the Binding using the Topic Exchange and the Movie.* routing key
    // This tells RabbitMQ to deliver messages matching 'Movie.*' to this queue.
    @Bean
    public Binding binding(Queue consumerQueue, TopicExchange eventExchange) {
        return BindingBuilder
                .bind(consumerQueue)
                .to(eventExchange)
                .with(routingKey);
    }

}
