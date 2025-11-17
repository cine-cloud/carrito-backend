package com.unrn.carritos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

   
    @Value("${rabbitmq.event.exchange.name}")
    private String eventExchange;

    @Value("${rabbitmq.event.consumer.queue.name}")
    private String queueName;

    @Value("${rabbitmq.event.movie.routing.key}")
    private String routingKey;

   
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
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
                .with(routingKey);
    }

}
