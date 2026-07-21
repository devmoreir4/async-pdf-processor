package com.rmq.devmoreir4.publisher.controller;

import com.rmq.devmoreir4.publisher.model.DocumentJob;
import com.rmq.devmoreir4.publisher.service.DocumentService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(path = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentSubmissionResponse> submit(@RequestPart("file") MultipartFile file) {
        DocumentJob job = documentService.submit(file);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(job.getId())
                .toUri();
        return ResponseEntity.accepted()
                .location(location)
                .body(new DocumentSubmissionResponse(job.getId(), job.getStatus().name(), location.toString()));
    }

    @GetMapping("/documents/{id}")
    public DocumentStatusResponse status(@PathVariable UUID id) {
        return DocumentStatusResponse.from(documentService.find(id));
    }

    public record DocumentSubmissionResponse(UUID documentId, String status, String statusUrl) {
    }

    public record DocumentStatusResponse(
            UUID documentId,
            String originalFilename,
            String status,
            String extractedText,
            ErrorResponse error,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

        static DocumentStatusResponse from(DocumentJob job) {
            ErrorResponse error = job.getErrorCode() == null
                    ? null
                    : new ErrorResponse(job.getErrorCode(), job.getErrorMessage());
            return new DocumentStatusResponse(
                    job.getId(),
                    job.getOriginalFilename(),
                    job.getStatus().name(),
                    job.getExtractedText(),
                    error,
                    job.getCreatedAt(),
                    job.getUpdatedAt());
        }
    }

    public record ErrorResponse(String code, String message) {
    }
}
