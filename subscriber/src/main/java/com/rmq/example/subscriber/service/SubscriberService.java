package com.rmq.example.subscriber.service;

import com.rmq.example.subscriber.model.QueueMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SubscriberService {

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveMessage(QueueMessage message) {

        try {
            System.out.println("Received message:");
            System.out.println("  ID: " + message.getId());
            System.out.println("  Sender: " + message.getSender());
            System.out.println("  Content: " + message.getContent());
            System.out.println("  Timestamp: " + message.getTimestamp());

            // Simulate failure for DLQ testing
            if ("fail".equalsIgnoreCase(message.getContent())) {
                throw new RuntimeException("Simulated failure for DLQ testing");
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            throw e;
        }
    }

}
