package com.rmq.devmoreir4.subscriber.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentProcessingMessage(
        UUID documentId,
        String storagePath,
        String originalFilename,
        OffsetDateTime createdAt) {
}
