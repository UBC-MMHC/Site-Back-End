package com.ubcmmhcsoftware.membership.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class RabbitMQConfig {

    @Value("${membership.events.exchange:membership.events}")
    private String exchange;

    @Value("${membership.events.queue.membership-created:membership.created}")
    private String queueName;

    @Value("${membership.events.routing-key.membership-created:membership.created}")
    private String routingKey;

    @Bean
    DirectExchange membershipEventsExchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    Queue membershipCreatedQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    Binding membershipCreatedBinding(Queue membershipCreatedQueue, DirectExchange membershipEventsExchange) {
        return BindingBuilder.bind(membershipCreatedQueue).to(membershipEventsExchange).with(routingKey);
    }
}
