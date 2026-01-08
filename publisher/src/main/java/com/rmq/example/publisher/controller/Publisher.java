package com.rmq.example.publisher.controller;

import com.rmq.example.publisher.model.QueueMessage;
import com.rmq.example.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
public class Publisher {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Autowired
    private PublisherService publisherService;

    @PostMapping("/publish/json")
    public void publishJson(@RequestBody QueueMessage message) {
        if (message.getId() == null)
            message.setId(UUID.randomUUID().toString());
        if (message.getTimestamp() == null)
            message.setTimestamp(LocalDateTime.now());

        System.out.println("Sending json message: " + message);
        publisherService.publishJsonMessage(message, queueName);
    }

}
