package com.rmq.example.subscriber.service;

import com.rmq.example.subscriber.model.QueueMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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
        @DisplayName("Should print message details to console")
        void receiveMessage_ShouldPrintMessageDetails() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outputStream));

            QueueMessage message = new QueueMessage("print-test-123", "Print test", "print-sender", TEST_TIMESTAMP);

            try {
                subscriberService.receiveMessage(message);
                String output = outputStream.toString();
                assertTrue(output.contains("Received message:"));
                assertTrue(output.contains("ID: print-test-123"));
                assertTrue(output.contains("Sender: print-sender"));
                assertTrue(output.contains("Content: Print test"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("Should print error when exception occurs")
        void receiveMessage_ShouldPrintErrorWhenExceptionOccurs() {
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;
            System.setErr(new PrintStream(errorStream));

            QueueMessage message = new QueueMessage("test-123", "fail", "test-sender", TEST_TIMESTAMP);

            try {
                assertThrows(RuntimeException.class, () -> subscriberService.receiveMessage(message));
                String errorOutput = errorStream.toString();
                assertTrue(errorOutput.contains("Error processing message:"));
            } finally {
                System.setErr(originalErr);
            }
        }
    }
}
