package com.rmq.devmoreir4.subscriber.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rmq.devmoreir4.subscriber.exception.PermanentProcessingException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfTextExtractorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void extractsNativeTextWithoutCallingTesseract() throws Exception {
        Path pdf = temporaryDirectory.resolve("native-text.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                content.showText("This PDF contains enough native text for direct extraction.");
                content.endText();
            }
            document.save(pdf.toFile());
        }
        PdfTextExtractor extractor = new PdfTextExtractor(
                "command-that-must-not-run", "por+eng", 20, 300);

        String result = extractor.extract(pdf);

        assertTrue(result.contains("enough native text"));
    }

    @Test
    void rejectsCorruptedPdfAsPermanentFailure() throws Exception {
        Path pdf = temporaryDirectory.resolve("corrupted.pdf");
        Files.writeString(pdf, "%PDF-invalid");
        PdfTextExtractor extractor = new PdfTextExtractor("tesseract", "por+eng", 20, 300);

        assertThrows(PermanentProcessingException.class, () -> extractor.extract(pdf));
    }
}
