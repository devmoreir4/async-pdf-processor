package com.rmq.example.publisher.service;

import com.rmq.example.publisher.model.QueueMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishJsonMessage(QueueMessage message, String queueName) {
        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("Published json message to " + queueName + ": " + message);
    }

}
