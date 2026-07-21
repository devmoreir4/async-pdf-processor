package com.rmq.devmoreir4.subscriber.service;

import com.rmq.devmoreir4.subscriber.exception.PermanentProcessingException;
import com.rmq.devmoreir4.subscriber.model.DocumentJob;
import com.rmq.devmoreir4.subscriber.model.DocumentProcessingMessage;
import com.rmq.devmoreir4.subscriber.model.DocumentStatus;
import com.rmq.devmoreir4.subscriber.repository.DocumentJobRepository;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DocumentProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessorService.class);
    private final DocumentJobRepository repository;
    private final PdfTextExtractor textExtractor;

    public DocumentProcessorService(DocumentJobRepository repository, PdfTextExtractor textExtractor) {
        this.repository = repository;
        this.textExtractor = textExtractor;
    }

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void process(DocumentProcessingMessage message) {
        DocumentJob job = repository.findById(message.documentId())
                .orElseThrow(() -> new PermanentProcessingException(
                        "Document job does not exist: " + message.documentId()));

        if (job.getStatus() == DocumentStatus.COMPLETED) {
            logger.info("Ignoring already completed document - ID: {}", message.documentId());
            return;
        }

        job.processing();
        repository.save(job);
        logger.info("Processing document - ID: {}, file: {}", message.documentId(), message.originalFilename());

        String text = textExtractor.extract(Path.of(message.storagePath()));
        job.complete(text);
        repository.save(job);
        logger.info("Document processed successfully - ID: {}", message.documentId());
    }
}
