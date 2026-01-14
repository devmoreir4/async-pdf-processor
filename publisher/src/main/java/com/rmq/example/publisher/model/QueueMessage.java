package com.rmq.example.publisher.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

public record QueueMessage(
    String id,
    String content,
    String sender,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp
) {

    public QueueMessage withDefaults() {
        return new QueueMessage(
            id != null ? id : UUID.randomUUID().toString(),
            content,
            sender,
            timestamp != null ? timestamp : LocalDateTime.now()
        );
    }
}
