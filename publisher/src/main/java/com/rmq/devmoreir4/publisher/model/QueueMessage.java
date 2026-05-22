package com.rmq.devmoreir4.publisher.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public record QueueMessage(
    String id,
    @NotBlank(message = "Message content cannot be empty")
    String content,
    @NotBlank(message = "Sender cannot be empty")
    String sender,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp
) {

    public QueueMessage withDefaults() {
        return new QueueMessage(
            id != null && !id.isBlank() ? id : UUID.randomUUID().toString(),
            content,
            sender,
            timestamp != null ? timestamp : LocalDateTime.now()
        );
    }
}
