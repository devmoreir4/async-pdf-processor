package com.rmq.devmoreir4.subscriber.service;

import com.rmq.devmoreir4.subscriber.model.QueueMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class SubscriberServiceTest {

    @InjectMocks
    private SubscriberService subscriberService;

    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2026, 1, 14, 10, 0, 0);

    @Nested
    @DisplayName("Successful processing tests")
    class SuccessfulProcessingTests {

        @Test
        @DisplayName("Should process valid message without exceptions")
        void receiveMessage_ShouldProcessValidMessageSuccessfully() {
            QueueMessage message = new QueueMessage("valid-123", "Valid content", "valid-sender", TEST_TIMESTAMP);
            assertDoesNotThrow(() -> subscriberService.receiveMessage(message));
        }

        @Test
        @DisplayName("Should not throw for content containing 'fail' but not exactly 'fail'")
        void receiveMessage_ShouldNotThrowExceptionForNonFailContent() {
            QueueMessage message = new QueueMessage("test-123", "failure", "test-sender", TEST_TIMESTAMP);
            assertDoesNotThrow(() -> subscriberService.receiveMessage(message));
        }
    }

    @Nested
    @DisplayName("DLQ failure tests")
    class DLQFailureTests {

        @Test
        @DisplayName("Should throw exception when content is 'fail'")
        void receiveMessage_ShouldThrowExceptionWhenContentIsFail() {
            QueueMessage message = new QueueMessage("test-123", "fail", "test-sender", TEST_TIMESTAMP);
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> subscriberService.receiveMessage(message));
            assertEquals("Simulated failure for DLQ testing", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when content is 'FAIL' (uppercase)")
        void receiveMessage_ShouldThrowExceptionWhenContentIsFailUpperCase() {
            QueueMessage message = new QueueMessage("test-123", "FAIL", "test-sender", TEST_TIMESTAMP);
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> subscriberService.receiveMessage(message));
            assertEquals("Simulated failure for DLQ testing", exception.getMessage());
        }

        @Test
        @DisplayName("Should re-throw exception (not just catch)")
        void receiveMessage_ShouldRethrowException() {
            QueueMessage message = new QueueMessage("test-123", "fail", "test-sender", TEST_TIMESTAMP);
            assertThrows(RuntimeException.class, () -> subscriberService.receiveMessage(message));
        }
    }

    @Nested
    @DisplayName("Null/empty content tests")
    class NullAndEmptyContentTests {

        @Test
        @DisplayName("Should process message with null content")
        void receiveMessage_ShouldProcessMessageWithNullContent() {
            QueueMessage message = new QueueMessage("test-123", null, "test-sender", TEST_TIMESTAMP);
            assertDoesNotThrow(() -> subscriberService.receiveMessage(message));
        }

        @Test
        @DisplayName("Should process message with empty content")
        void receiveMessage_ShouldProcessMessageWithEmptyContent() {
            QueueMessage message = new QueueMessage("test-123", "", "test-sender", TEST_TIMESTAMP);
            assertDoesNotThrow(() -> subscriberService.receiveMessage(message));
        }
    }

    @Nested
    @DisplayName("Output tests")
    class OutputTests {

        @Test
        @DisplayName("Should log message details")
        void receiveMessage_ShouldLogMessageDetails(CapturedOutput output) {
            QueueMessage message = new QueueMessage("print-test-123", "Print test", "print-sender", TEST_TIMESTAMP);

            subscriberService.receiveMessage(message);

            assertTrue(output.getAll().contains("Message received - ID: print-test-123, Sender: print-sender"));
            assertTrue(output.getAll().contains("Message processed successfully - ID: print-test-123"));
        }

        @Test
        @DisplayName("Should log error when exception occurs")
        void receiveMessage_ShouldLogErrorWhenExceptionOccurs(CapturedOutput output) {
            QueueMessage message = new QueueMessage("test-123", "fail", "test-sender", TEST_TIMESTAMP);

            assertThrows(RuntimeException.class, () -> subscriberService.receiveMessage(message));
            assertTrue(output.getAll().contains("Error processing message - ID: test-123"));
        }
    }
}
