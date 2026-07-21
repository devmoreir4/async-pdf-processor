package com.rmq.devmoreir4.publisher.service;

import com.rmq.devmoreir4.publisher.exception.InvalidDocumentException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageService {

    private static final byte[] PDF_SIGNATURE = {'%', 'P', 'D', 'F', '-'};
    private final Path storageRoot;
    private final long maxSizeBytes;

    public DocumentStorageService(
            @Value("${document.storage.root}") String storageRoot,
            @Value("${document.max-size-bytes}") long maxSizeBytes) {
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
        this.maxSizeBytes = maxSizeBytes;
    }

    public Path store(UUID documentId, MultipartFile file) {
        validate(file);
        try {
            Files.createDirectories(storageRoot);
            Path target = storageRoot.resolve(documentId + ".pdf").normalize();
            if (!target.startsWith(storageRoot)) {
                throw new InvalidDocumentException("Invalid storage path");
            }
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store the PDF", exception);
        }
    }

    public void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // The database status still records the failed upload.
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("A non-empty PDF file is required");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidDocumentException("PDF exceeds the maximum size of " + maxSizeBytes + " bytes");
        }
        try (InputStream input = file.getInputStream()) {
            byte[] signature = input.readNBytes(PDF_SIGNATURE.length);
            if (signature.length != PDF_SIGNATURE.length) {
                throw new InvalidDocumentException("Invalid PDF content");
            }
            for (int index = 0; index < PDF_SIGNATURE.length; index++) {
                if (signature[index] != PDF_SIGNATURE[index]) {
                    throw new InvalidDocumentException("Invalid PDF content");
                }
            }
        } catch (IOException exception) {
            throw new InvalidDocumentException("Could not read the uploaded file");
        }
    }
}
