package com.rmq.devmoreir4.publisher.controller;

import com.rmq.devmoreir4.publisher.model.QueueMessage;
import com.rmq.devmoreir4.publisher.service.PublisherService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Publisher {

    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);
    private final String queueName;
    private final PublisherService publisherService;

    public Publisher(
            @Value("${rabbitmq.queue.name}") String queueName,
            PublisherService publisherService) {
        this.queueName = queueName;
        this.publisherService = publisherService;
    }

    @PostMapping("/publish/json")
    public ResponseEntity<String> publishJson(@Valid @RequestBody QueueMessage message) {
        QueueMessage finalMessage = message.withDefaults();
        publisherService.publishJsonMessage(finalMessage, queueName);
        logger.info("Message sent successfully via /publish/json endpoint");
        return ResponseEntity.ok("Message published successfully - ID: " + finalMessage.id());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Publisher service is healthy");
    }
}
