package com.rmq.example.publisher.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishMessage(String message, String queueName) {
        System.out.println("Published message to " + queueName + ": " + message);
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
