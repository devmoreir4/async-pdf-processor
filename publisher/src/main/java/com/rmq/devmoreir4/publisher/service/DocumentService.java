package com.rmq.devmoreir4.publisher.service;

import com.rmq.devmoreir4.publisher.exception.DocumentNotFoundException;
import com.rmq.devmoreir4.publisher.exception.QueueUnavailableException;
import com.rmq.devmoreir4.publisher.model.DocumentJob;
import com.rmq.devmoreir4.publisher.model.DocumentProcessingMessage;
import com.rmq.devmoreir4.publisher.repository.DocumentJobRepository;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentJobRepository repository;
    private final DocumentStorageService storageService;
    private final PublisherService publisherService;

    public DocumentService(
            DocumentJobRepository repository,
            DocumentStorageService storageService,
            PublisherService publisherService) {
        this.repository = repository;
        this.storageService = storageService;
        this.publisherService = publisherService;
    }

    public DocumentJob submit(MultipartFile file) {
        UUID id = UUID.randomUUID();
        Path storedPath = storageService.store(id, file);
        OffsetDateTime now = OffsetDateTime.now();
        String originalFilename = safeFilename(file.getOriginalFilename());
        DocumentJob job = repository.save(new DocumentJob(id, originalFilename, storedPath.toString(), now));

        try {
            publisherService.publish(new DocumentProcessingMessage(id, storedPath.toString(), originalFilename, now));
            return job;
        } catch (RuntimeException exception) {
            job.fail("QUEUE_UNAVAILABLE", "Could not enqueue document for processing");
            repository.save(job);
            storageService.deleteQuietly(storedPath);
            throw new QueueUnavailableException("RabbitMQ is unavailable", exception);
        }
    }

    public DocumentJob find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "document.pdf";
        }
        String sanitized = filename.replace('\\', '/');
        sanitized = sanitized.substring(sanitized.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}:*?\"<>|]", "_")
                .strip();
        if (sanitized.isBlank()) {
            sanitized = "document.pdf";
        }
        return sanitized.length() <= 255 ? sanitized : sanitized.substring(0, 255);
    }
}
