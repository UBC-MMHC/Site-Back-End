package com.ubcmmhcsoftware.membership.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class RabbitMQConfig {

    @Value("${membership.events.exchange:membership.events}")
    private String exchange;

    /**
     * TopicExchange to align with Newsletter Service.
     * Routing keys: membership.created, membership.activated
     */
    @Bean
    TopicExchange membershipEventsExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
