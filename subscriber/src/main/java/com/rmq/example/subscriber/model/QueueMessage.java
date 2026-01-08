package com.rmq.example.subscriber.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueueMessage {

    private String id;
    private String content;
    private String sender;
    private java.time.LocalDateTime timestamp;

}
