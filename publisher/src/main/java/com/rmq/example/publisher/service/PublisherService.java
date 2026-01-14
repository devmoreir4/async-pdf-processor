package com.rmq.example.publisher.service;

import com.rmq.example.publisher.model.QueueMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    private final RabbitTemplate rabbitTemplate;

    public PublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishJsonMessage(QueueMessage message, String queueName) {
        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("Published message to " + queueName + ": " + message);
    }
}
