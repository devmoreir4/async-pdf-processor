package com.rmq.devmoreir4.publisher.exception;

public class QueueUnavailableException extends RuntimeException {
    public QueueUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
