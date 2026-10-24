package com.example.toeic_api.modules.vocabulary.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.toeic_api.modules.ai.service.GroqVocabularyService;
import com.example.toeic_api.shared.exception.AppException;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.imageio.ImageIO;

class VocabularyDocumentServiceTest {
    final GroqVocabularyService ai = mock(GroqVocabularyService.class);
    final VocabularyDocumentService service = new VocabularyDocumentService(ai);

    @Test
    void extractsTextPdfAndReportsSelectedPageRangeWithoutVision() throws Exception {
        var out = new ByteArrayOutputStream();
        try (var pdf = new PDDocument()) {
            for (int i = 0; i < 2; i++) {
                var page = new PDPage();
                pdf.addPage(page);
                try (var stream = new PDPageContentStream(pdf, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.newLineAtOffset(40, 700);
                    stream.showText(
                            "Please review the invoice and confirm the payment before the deadline."
                                    + " The supplier will deliver the package.");
                    stream.endText();
                }
            }
            pdf.save(out);
        }
        when(ai.extract(anyString(), isNull())).thenReturn(List.of("invoice", "deadline"));
        var response =
                service.extract(
                        new MockMultipartFile(
                                "file", "source.pdf", "application/pdf", out.toByteArray()),
                        2,
                        3);
        assertEquals(2, response.totalPages());
        assertEquals(2, response.fromPage());
        assertEquals(2, response.toPage());
        assertEquals(List.of("invoice", "deadline"), response.terms());
        verify(ai).extract(contains("invoice"), isNull());
    }

    @Test
    void imagesAndScannedPdfUseVision() throws Exception {
        when(ai.extract(isNull(), startsWith("data:image/jpeg;base64,")))
                .thenReturn(List.of("invoice"));
        var image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        var out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        assertEquals(
                List.of("invoice"),
                service.extract(
                                new MockMultipartFile(
                                        "file", "scan.png", "image/png", out.toByteArray()),
                                1,
                                3)
                        .terms());
        out.reset();
        try (var pdf = new PDDocument()) {
            pdf.addPage(new PDPage());
            pdf.save(out);
        }
        assertEquals(
                List.of("invoice"),
                service.extract(
                                new MockMultipartFile(
                                        "file", "scan.pdf", "application/pdf", out.toByteArray()),
                                1,
                                3)
                        .terms());
        verify(ai, times(2)).extract(isNull(), startsWith("data:image/jpeg;base64,"));
    }

    @Test
    void rejectsUnsupportedFilesInvalidRangesAndMalformedUtf8BeforeProvider() {
        assertThrows(
                AppException.class,
                () ->
                        service.extract(
                                new MockMultipartFile(
                                        "file", "bad.pdf", "application/pdf", "not PDF".getBytes()),
                                1,
                                3));
        assertThrows(
                AppException.class,
                () ->
                        service.extract(
                                new MockMultipartFile(
                                        "file", "file.txt", "text/plain", new byte[] {(byte) 0xff}),
                                1,
                                3));
        assertThrows(
                AppException.class,
                () ->
                        service.extract(
                                new MockMultipartFile(
                                        "file", "file.txt", "text/plain", "invoice".getBytes()),
                                1,
                                8));
        verifyNoInteractions(ai);
    }

    @Test
    void txtIsReadAsUtf8AndNotPersisted() {
        when(ai.extract(anyString(), isNull())).thenReturn(List.of("invoice"));
        var result =
                service.extract(
                        new MockMultipartFile(
                                "file",
                                "vocab.txt",
                                "text/plain",
                                "invoice — hóa đơn".getBytes(StandardCharsets.UTF_8)),
                        1,
                        3);
        assertEquals(List.of("invoice"), result.terms());
    }
}
