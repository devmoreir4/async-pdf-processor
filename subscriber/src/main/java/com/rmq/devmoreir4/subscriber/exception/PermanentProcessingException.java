package com.rmq.devmoreir4.subscriber.exception;

public class PermanentProcessingException extends RuntimeException {
    public PermanentProcessingException(String message) {
        super(message);
    }

    public PermanentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
