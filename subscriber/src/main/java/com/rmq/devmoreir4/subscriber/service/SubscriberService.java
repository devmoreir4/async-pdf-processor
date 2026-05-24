package com.rmq.devmoreir4.subscriber.service;

import com.rmq.devmoreir4.subscriber.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SubscriberService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriberService.class);

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveMessage(QueueMessage message) {
        try {
            logger.info("Message received - ID: {}, Sender: {}", message.id(), message.sender());
            logger.debug("Message content: {}, Timestamp: {}", message.content(), message.timestamp());

            if ("fail".equalsIgnoreCase(message.content())) {
                throw new RuntimeException("Simulated failure for DLQ testing");
            }

            logger.info("Message processed successfully - ID: {}", message.id());
        } catch (RuntimeException e) {
            logger.error("Error processing message - ID: {}", message.id(), e);
            throw e;
        }
    }
}
