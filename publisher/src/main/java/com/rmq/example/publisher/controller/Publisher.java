package com.rmq.example.publisher.controller;

import com.rmq.example.publisher.model.QueueMessage;
import com.rmq.example.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Publisher {

    private final String queueName;
    private final PublisherService publisherService;

    public Publisher(
            @Value("${rabbitmq.queue.name}") String queueName,
            PublisherService publisherService) {
        this.queueName = queueName;
        this.publisherService = publisherService;
    }

    @PostMapping("/publish/json")
    public void publishJson(@RequestBody QueueMessage message) {
        QueueMessage finalMessage = message.withDefaults();
        System.out.println("Sending message: " + finalMessage);
        publisherService.publishJsonMessage(finalMessage, queueName);
    }
}
