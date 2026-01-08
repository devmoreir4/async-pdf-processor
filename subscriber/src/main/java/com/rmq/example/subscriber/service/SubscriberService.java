package com.rmq.example.subscriber.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmq.example.subscriber.model.QueueMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class SubscriberService {

    ObjectMapper objectMapper;

    @RabbitListener(containerFactory = "listenerContainerFactory", queues = "${rabbitmq.queue.name}")
    public void receiveMessage(Message message) {
        String jsonMessage = rmqMessageToString(message);
        QueueMessage msgObject = (QueueMessage) jsonToObject(jsonMessage, QueueMessage.class);
        System.out.println("Received message: " + msgObject.toString()) ;
    }

    private String rmqMessageToString(Message message) {
        return new String(message.getBody(), StandardCharsets.UTF_8);
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
