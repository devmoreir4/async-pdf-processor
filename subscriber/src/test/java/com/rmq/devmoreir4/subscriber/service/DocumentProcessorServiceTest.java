package com.rmq.devmoreir4.subscriber.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmq.devmoreir4.subscriber.model.DocumentJob;
import com.rmq.devmoreir4.subscriber.model.DocumentProcessingMessage;
import com.rmq.devmoreir4.subscriber.model.DocumentStatus;
import com.rmq.devmoreir4.subscriber.repository.DocumentJobRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentProcessorServiceTest {

    @Mock
    private DocumentJobRepository repository;

    @Mock
    private PdfTextExtractor textExtractor;

    @Mock
    private DocumentJob job;

    @Test
    void processesAndCompletesQueuedDocument() {
        UUID id = UUID.randomUUID();
        DocumentProcessingMessage message =
                new DocumentProcessingMessage(id, "storage/test.pdf", "test.pdf", OffsetDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(job));
        when(job.getStatus()).thenReturn(DocumentStatus.QUEUED);
        when(textExtractor.extract(java.nio.file.Path.of("storage/test.pdf"))).thenReturn("extracted text");
        DocumentProcessorService service = new DocumentProcessorService(repository, textExtractor);

        service.process(message);

        verify(job).processing();
        verify(job).complete("extracted text");
        verify(repository, org.mockito.Mockito.times(2)).save(job);
    }

    @Test
    void ignoresAlreadyCompletedDocument() {
        UUID id = UUID.randomUUID();
        DocumentProcessingMessage message =
                new DocumentProcessingMessage(id, "storage/test.pdf", "test.pdf", OffsetDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(job));
        when(job.getStatus()).thenReturn(DocumentStatus.COMPLETED);
        DocumentProcessorService service = new DocumentProcessorService(repository, textExtractor);

        service.process(message);

        verify(textExtractor, never()).extract(java.nio.file.Path.of("storage/test.pdf"));
        verify(repository, never()).save(job);
    }
}
