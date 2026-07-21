package com.rmq.devmoreir4.publisher.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;

import com.rmq.devmoreir4.publisher.model.DocumentProcessingMessage;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitOperations;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishesDocumentMessageToConfiguredQueue() {
        PublisherService service = new PublisherService(rabbitTemplate, "pdf.processing");
        DocumentProcessingMessage message = new DocumentProcessingMessage(
                UUID.randomUUID(), "storage/document.pdf", "document.pdf", OffsetDateTime.now());
        doAnswer(invocation -> {
            RabbitOperations.OperationsCallback<?> callback = invocation.getArgument(0);
            return callback.doInRabbit(rabbitTemplate);
        }).when(rabbitTemplate).invoke(any(RabbitOperations.OperationsCallback.class));

        service.publish(message);

        verify(rabbitTemplate).convertAndSend("pdf.processing", message);
        verify(rabbitTemplate).waitForConfirmsOrDie(5000);
    }
}
