package com.rmq.devmoreir4.subscriber.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_jobs")
public class DocumentJob {

    @Id
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "storage_path", nullable = false, length = 1024)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected DocumentJob() {
    }

    public void processing() {
        status = DocumentStatus.PROCESSING;
        errorCode = null;
        errorMessage = null;
        updatedAt = OffsetDateTime.now();
    }

    public void complete(String text) {
        status = DocumentStatus.COMPLETED;
        extractedText = text;
        errorCode = null;
        errorMessage = null;
        updatedAt = OffsetDateTime.now();
    }

    public void fail(String code, String message) {
        status = DocumentStatus.FAILED;
        errorCode = code;
        errorMessage = message;
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public DocumentStatus getStatus() { return status; }
}
