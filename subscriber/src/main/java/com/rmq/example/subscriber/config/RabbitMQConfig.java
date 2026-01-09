package com.rmq.example.subscriber.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Bean
    public Queue createQueue() {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange("")
                .deadLetterRoutingKey(queueName + ".dlq")
                .build();
    }

    @Bean
    public Queue boqQueue() {
        return QueueBuilder.durable("boq." + queueName)
                .deadLetterExchange("")
                .deadLetterRoutingKey(queueName + ".dlq")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public MessageConverter converter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

}
