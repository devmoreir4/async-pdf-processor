package com.rmq.devmoreir4.publisher.service;

import com.rmq.devmoreir4.publisher.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    private static final Logger logger = LoggerFactory.getLogger(PublisherService.class);
    private final RabbitTemplate rabbitTemplate;

    public PublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishJsonMessage(QueueMessage message, String queueName) {
        try {
            rabbitTemplate.convertAndSend(queueName, message);
            logger.info("Message published successfully to queue: {} - ID: {}", queueName, message.id());
        } catch (RuntimeException e) {
            logger.error("Failed to publish message to queue: {}", queueName, e);
            throw e;
        }
    }
}
