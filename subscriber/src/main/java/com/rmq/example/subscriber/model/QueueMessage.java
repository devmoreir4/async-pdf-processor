package com.rmq.example.subscriber.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record QueueMessage(
    String id,
    String content,
    String sender,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp
) {}
