package com.rmq.devmoreir4.subscriber.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmq.devmoreir4.subscriber.exception.PermanentProcessingException;
import com.rmq.devmoreir4.subscriber.model.DocumentProcessingMessage;
import com.rmq.devmoreir4.subscriber.repository.DocumentJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.stereotype.Component;

@Component
public class DocumentFailureRecoverer implements MessageRecoverer {

    private static final Logger logger = LoggerFactory.getLogger(DocumentFailureRecoverer.class);
    private final DocumentJobRepository repository;
    private final ObjectMapper objectMapper;
    private final RejectAndDontRequeueRecoverer delegate = new RejectAndDontRequeueRecoverer();

    public DocumentFailureRecoverer(DocumentJobRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void recover(Message message, Throwable cause) {
        try {
            DocumentProcessingMessage payload =
                    objectMapper.readValue(message.getBody(), DocumentProcessingMessage.class);
            repository.findById(payload.documentId()).ifPresent(job -> {
                Throwable rootCause = rootCause(cause);
                String code = contains(cause, PermanentProcessingException.class)
                        ? "INVALID_DOCUMENT"
                        : "PROCESSING_FAILED";
                job.fail(code, sanitizedMessage(rootCause));
                repository.save(job);
            });
        } catch (Exception recoveryException) {
            logger.error("Could not update failed document status", recoveryException);
        }
        delegate.recover(message, cause);
    }

    private boolean contains(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private String sanitizedMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return "Document processing failed";
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
