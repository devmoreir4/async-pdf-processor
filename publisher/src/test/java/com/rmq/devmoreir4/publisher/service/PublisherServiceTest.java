package com.rmq.devmoreir4.publisher.service;

import com.rmq.devmoreir4.publisher.model.QueueMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PublisherService publisherService;

    private QueueMessage testMessage;
    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2026, 1, 14, 10, 0, 0);

    @BeforeEach
    void setUp() {
        testMessage = new QueueMessage("test-123", "Test message content", "test-sender", TEST_TIMESTAMP);
    }

    @Nested
    @DisplayName("Basic publish tests")
    class BasicPublishTests {

        @Test
        @DisplayName("Should call RabbitTemplate with correct parameters")
        void publishJsonMessage_ShouldCallRabbitTemplateWithCorrectParameters() {
            String queueName = "test_queue";
            publisherService.publishJsonMessage(testMessage, queueName);
            verify(rabbitTemplate, times(1)).convertAndSend(queueName, testMessage);
        }

        @Test
        @DisplayName("Should publish to different queues")
        void publishJsonMessage_ShouldPublishToDifferentQueues() {
            String queue1 = "queue_one";
            String queue2 = "queue_two";

            publisherService.publishJsonMessage(testMessage, queue1);
            publisherService.publishJsonMessage(testMessage, queue2);

            verify(rabbitTemplate, times(1)).convertAndSend(queue1, testMessage);
            verify(rabbitTemplate, times(1)).convertAndSend(queue2, testMessage);
        }

        @Test
        @DisplayName("Should publish different messages")
        void publishJsonMessage_ShouldPublishDifferentMessages() {
            String queueName = "test_queue";
            QueueMessage message1 = new QueueMessage("msg-1", "First message", "sender-1", TEST_TIMESTAMP);
            QueueMessage message2 = new QueueMessage("msg-2", "Second message", "sender-2", TEST_TIMESTAMP);

            publisherService.publishJsonMessage(message1, queueName);
            publisherService.publishJsonMessage(message2, queueName);

            verify(rabbitTemplate, times(1)).convertAndSend(queueName, message1);
            verify(rabbitTemplate, times(1)).convertAndSend(queueName, message2);
        }
    }

    @Nested
    @DisplayName("Null/empty content tests")
    class NullAndEmptyContentTests {

        @Test
        @DisplayName("Should handle null content")
        void publishJsonMessage_ShouldHandleNullContent() {
            String queueName = "test_queue";
            QueueMessage message = new QueueMessage("test-null", null, "test-sender", TEST_TIMESTAMP);
            publisherService.publishJsonMessage(message, queueName);
            verify(rabbitTemplate, times(1)).convertAndSend(queueName, message);
        }

        @Test
        @DisplayName("Should handle empty content")
        void publishJsonMessage_ShouldHandleEmptyContent() {
            String queueName = "test_queue";
            QueueMessage message = new QueueMessage("test-empty", "", "test-sender", TEST_TIMESTAMP);
            publisherService.publishJsonMessage(message, queueName);
            verify(rabbitTemplate, times(1)).convertAndSend(queueName, message);
        }
    }

    @Nested
    @DisplayName("Record feature tests")
    class RecordFeatureTests {

        @Test
        @DisplayName("Should generate id and timestamp with withDefaults()")
        void queueMessage_ShouldGenerateDefaults() {
            QueueMessage message = new QueueMessage(null, "content", "sender", null);
            QueueMessage result = message.withDefaults();

            assertNotNull(result.id());
            assertNotNull(result.timestamp());
            assertEquals("content", result.content());
            assertEquals("sender", result.sender());
        }

        @Test
        @DisplayName("Should keep existing id and timestamp with withDefaults()")
        void queueMessage_ShouldKeepExistingValues() {
            QueueMessage message = new QueueMessage("existing-id", "content", "sender", TEST_TIMESTAMP);
            QueueMessage result = message.withDefaults();

            assertEquals("existing-id", result.id());
            assertEquals(TEST_TIMESTAMP, result.timestamp());
        }

        @Test
        @DisplayName("Should generate id when id is blank")
        void queueMessage_ShouldGenerateIdWhenBlank() {
            QueueMessage message = new QueueMessage(" ", "content", "sender", TEST_TIMESTAMP);
            QueueMessage result = message.withDefaults();

            assertNotNull(result.id());
            assertFalse(result.id().isBlank());
            assertEquals(TEST_TIMESTAMP, result.timestamp());
        }
    }

    @Nested
    @DisplayName("Validation tests")
    class ValidationTests {

        private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        @Test
        @DisplayName("Should accept request payload without id")
        void queueMessage_ShouldAcceptPayloadWithoutId() {
            QueueMessage message = new QueueMessage(null, "content", "sender", null);

            Set<ConstraintViolation<QueueMessage>> violations = validator.validate(message);

            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject blank content and sender")
        void queueMessage_ShouldRejectBlankContentAndSender() {
            QueueMessage message = new QueueMessage(null, "", " ", null);

            Set<ConstraintViolation<QueueMessage>> violations = validator.validate(message);

            assertEquals(2, violations.size());
        }
    }
}
