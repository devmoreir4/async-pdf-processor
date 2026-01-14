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
            System.out.println("  ID: " + message.id());
            System.out.println("  Sender: " + message.sender());
            System.out.println("  Content: " + message.content());
            System.out.println("  Timestamp: " + message.timestamp());

            // Simulate failure to test DLQ
            if ("fail".equalsIgnoreCase(message.content())) {
                throw new RuntimeException("Simulated failure for DLQ testing");
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            throw e; // Re-throw so retry/DLQ works
        }
    }
}
