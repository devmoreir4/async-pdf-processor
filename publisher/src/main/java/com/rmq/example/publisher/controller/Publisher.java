package com.rmq.example.publisher.controller;

import com.rmq.example.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Publisher {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Autowired
    private PublisherService publisherService;

    @PostMapping("/publish/text")
    public void publishText(@RequestBody String message) {
        System.out.println("Sending text message: " + message);
        publisherService.publishTextMessage(message, queueName);
    }

    @PostMapping("/publish/json")
    public void publishJson(@RequestBody String message) {
        System.out.println("Sending json message: " + message);
        publisherService.publishJsonMessage(message, queueName);
    }

}
