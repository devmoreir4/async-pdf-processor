package com.rmq.devmoreir4.subscriber.exception;

public class TransientProcessingException extends RuntimeException {
    public TransientProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransientProcessingException(String message) {
        super(message);
    }
}
