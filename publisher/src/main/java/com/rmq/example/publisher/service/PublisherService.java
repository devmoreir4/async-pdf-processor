package com.rmq.example.publisher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmq.example.publisher.model.QueueMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishTextMessage(String message, String queueName) {
        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("Published text message to " + queueName + ": " + message);
    }

    public void publishJsonMessage(String message, String queueName) {
        QueueMessage msgObject = (QueueMessage) jsonToObject(message, QueueMessage.class);
        rabbitTemplate.convertAndSend(queueName, msgObject);
        System.out.println("Published json message to " + queueName + ": " + objectToJNode(msgObject));
    }

    private JsonNode objectToJNode(Object jsonObject) {
        return objectMapper.valueToTree(jsonObject);
    }

    private Object jsonToObject(String jsonString, Class<?> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
    }

}
