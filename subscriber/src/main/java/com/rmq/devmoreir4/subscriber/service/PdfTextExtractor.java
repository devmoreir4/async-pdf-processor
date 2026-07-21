package com.rmq.devmoreir4.subscriber.service;

import com.rmq.devmoreir4.subscriber.exception.PermanentProcessingException;
import com.rmq.devmoreir4.subscriber.exception.TransientProcessingException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PdfTextExtractor {

    private final String tesseractCommand;
    private final String languages;
    private final int minimumCharacters;
    private final int renderDpi;

    public PdfTextExtractor(
            @Value("${ocr.tesseract.command}") String tesseractCommand,
            @Value("${ocr.tesseract.languages}") String languages,
            @Value("${ocr.text.minimum-characters-per-page}") int minimumCharacters,
            @Value("${ocr.render-dpi}") int renderDpi) {
        this.tesseractCommand = tesseractCommand;
        this.languages = languages;
        this.minimumCharacters = minimumCharacters;
        this.renderDpi = renderDpi;
    }

    public String extract(Path pdfPath) {
        if (!Files.isRegularFile(pdfPath)) {
            throw new TransientProcessingException("Stored PDF is not available: " + pdfPath);
        }

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            if (document.isEncrypted()) {
                throw new PermanentProcessingException("Encrypted PDFs are not supported");
            }
            if (document.getNumberOfPages() == 0) {
                throw new PermanentProcessingException("PDF does not contain pages");
            }

            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder result = new StringBuilder();
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                String nativeText = extractPageText(document, pageIndex);
                String pageText = usefulCharacters(nativeText) >= minimumCharacters
                        ? nativeText
                        : runOcr(renderer, pageIndex);
                if (!pageText.isBlank()) {
                    if (!result.isEmpty()) {
                        result.append(System.lineSeparator()).append(System.lineSeparator());
                    }
                    result.append(pageText.strip());
                }
            }
            return result.toString();
        } catch (PermanentProcessingException | TransientProcessingException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new PermanentProcessingException("Invalid or corrupted PDF", exception);
        } catch (SecurityException exception) {
            throw new PermanentProcessingException("PDF cannot be opened", exception);
        }
    }

    private String extractPageText(PDDocument document, int pageIndex) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        return stripper.getText(document);
    }

    private String runOcr(PDFRenderer renderer, int pageIndex) {
        Path temporaryDirectory = null;
        try {
            temporaryDirectory = Files.createTempDirectory("pdf-ocr-");
            Path imagePath = temporaryDirectory.resolve("page-" + (pageIndex + 1) + ".png");
            Path outputBase = temporaryDirectory.resolve("result");
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, renderDpi, ImageType.RGB);
            ImageIO.write(image, "png", imagePath.toFile());

            Process process;
            try {
                process = new ProcessBuilder(
                        tesseractCommand,
                        imagePath.toString(),
                        outputBase.toString(),
                        "-l",
                        languages)
                        .redirectErrorStream(true)
                        .start();
            } catch (IOException exception) {
                throw new PermanentProcessingException(
                        "Tesseract binary '" + tesseractCommand + "' could not be started - check OCR configuration",
                        exception);
            }

            boolean finished = process.waitFor(Duration.ofMinutes(2).toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new TransientProcessingException("Tesseract timed out on page " + (pageIndex + 1));
            }
            String processOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                throw new TransientProcessingException(
                        "Tesseract failed on page " + (pageIndex + 1) + ": " + processOutput.strip());
            }
            return Files.readString(Path.of(outputBase + ".txt"), StandardCharsets.UTF_8);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TransientProcessingException("OCR processing was interrupted", exception);
        } catch (IOException exception) {
            throw new TransientProcessingException("Could not execute Tesseract", exception);
        } finally {
            deleteDirectoryQuietly(temporaryDirectory);
        }
    }

    private long usefulCharacters(String value) {
        return value.codePoints().filter(codePoint -> !Character.isWhitespace(codePoint)).count();
    }

    private void deleteDirectoryQuietly(Path directory) {
        if (directory == null) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            paths.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Temporary OCR artifacts can be cleaned by the operating system.
                }
            });
        } catch (IOException ignored) {
            // Temporary OCR artifacts can be cleaned by the operating system.
        }
    }
}
