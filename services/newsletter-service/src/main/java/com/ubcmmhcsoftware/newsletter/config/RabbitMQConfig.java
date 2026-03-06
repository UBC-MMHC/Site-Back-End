package com.ubcmmhcsoftware.newsletter.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_MEMBERSHIP_EVENTS = "membership.events";
    public static final String ROUTING_KEY_MEMBERSHIP_CREATED = "membership.created";
    public static final String QUEUE_NEWSLETTER_MEMBERSHIP_CREATED = "newsletter.membership-created";

    @Bean
    public TopicExchange membershipEventsExchange() {
        return new TopicExchange(EXCHANGE_MEMBERSHIP_EVENTS, true, false);
    }

    @Bean
    public Queue newsletterMembershipCreatedQueue() {
        return new Queue(QUEUE_NEWSLETTER_MEMBERSHIP_CREATED, true);
    }

    @Bean
    public Binding newsletterMembershipCreatedBinding(
            Queue newsletterMembershipCreatedQueue,
            TopicExchange membershipEventsExchange) {
        return BindingBuilder.bind(newsletterMembershipCreatedQueue)
                .to(membershipEventsExchange)
                .with(ROUTING_KEY_MEMBERSHIP_CREATED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
