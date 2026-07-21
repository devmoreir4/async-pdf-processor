package com.rmq.devmoreir4.publisher.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rmq.devmoreir4.publisher.exception.InvalidDocumentException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class DocumentStorageServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void storesPdfUsingGeneratedDocumentId() throws Exception {
        byte[] content = "%PDF-1.7\nsample".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", content);
        UUID id = UUID.randomUUID();
        DocumentStorageService service = new DocumentStorageService(temporaryDirectory.toString(), 1024);

        Path stored = service.store(id, file);

        assertArrayEquals(content, Files.readAllBytes(stored));
    }

    @Test
    void rejectsContentWithoutPdfSignature() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", "not a pdf".getBytes());
        DocumentStorageService service = new DocumentStorageService(temporaryDirectory.toString(), 1024);

        assertThrows(InvalidDocumentException.class, () -> service.store(UUID.randomUUID(), file));
    }

    @Test
    void rejectsOversizedPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", "%PDF-too-large".getBytes());
        DocumentStorageService service = new DocumentStorageService(temporaryDirectory.toString(), 5);

        assertThrows(InvalidDocumentException.class, () -> service.store(UUID.randomUUID(), file));
    }
}
