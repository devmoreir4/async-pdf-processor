package com.rmq.devmoreir4.subscriber.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record QueueMessage(
    @NotBlank(message = "ID cannot be blank")
    String id,
    @NotBlank(message = "Message content cannot be empty")
    String content,
    @NotBlank(message = "Sender cannot be empty")
    String sender,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp
) {}
