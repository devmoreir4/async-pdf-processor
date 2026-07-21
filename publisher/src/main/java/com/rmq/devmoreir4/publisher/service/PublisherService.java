package com.rmq.devmoreir4.publisher.service;

import com.rmq.devmoreir4.publisher.model.DocumentProcessingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    private static final Logger logger = LoggerFactory.getLogger(PublisherService.class);
    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public PublisherService(
            RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.queue.name}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    public void publish(DocumentProcessingMessage message) {
        rabbitTemplate.invoke(operations -> {
            operations.convertAndSend(queueName, message);
            operations.waitForConfirmsOrDie(5000);
            return null;
        });
        logger.info("Document queued - ID: {}, queue: {}", message.documentId(), queueName);
    }
}
