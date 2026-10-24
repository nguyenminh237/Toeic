package com.example.toeic_api.modules.vocabulary.service;

import com.example.toeic_api.modules.ai.service.GroqVocabularyService;
import com.example.toeic_api.shared.exception.*;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;

@Service
public class VocabularyDocumentService {
    private final GroqVocabularyService ai;

    public VocabularyDocumentService(GroqVocabularyService ai) {
        this.ai = ai;
    }

    public record Extraction(
            List<String> terms, int totalPages, int fromPage, int toPage, boolean limited) {}

    public Extraction extract(MultipartFile file, int from, int to) {
        if (file == null || file.isEmpty()) throw new AppException(ErrorCode.FILE_REQUIRED);
        if (file.getSize() > 10 * 1024 * 1024)
            throw new AppException(ErrorCode.FILE_TOO_LARGE, "Tài liệu tối đa 10 MB.");
        if (from < 1 || to < from || to - from >= 5)
            throw invalid("Chọn từ 1 đến 5 trang mỗi lần trích xuất.");
        try {
            byte[] bytes = file.getBytes();
            var terms = new LinkedHashSet<String>();
            int pages = 1, end = 1;
            String magic =
                    new String(bytes, 0, Math.min(bytes.length, 8), StandardCharsets.ISO_8859_1);
            if (magic.startsWith("%PDF-")) {
                try (var document = Loader.loadPDF(bytes)) {
                    pages = document.getNumberOfPages();
                    end = Math.min(to, pages);
                    if (from > pages) throw invalid("Trang bắt đầu vượt quá số trang của PDF.");
                    StringBuilder text = new StringBuilder();
                    for (int page = from; page <= end; page++) {
                        var stripper = new PDFTextStripper();
                        stripper.setStartPage(page);
                        stripper.setEndPage(page);
                        String content = stripper.getText(document);
                        if (content.strip().length() >= 80) text.append(content).append('\n');
                        else {
                            var box = document.getPage(page - 1).getCropBox();
                            float longest = Math.max(box.getWidth(), box.getHeight());
                            if (longest <= 0 || !Float.isFinite(longest))
                                throw invalid("Kích thước trang PDF không hợp lệ.");
                            var image =
                                    new PDFRenderer(document)
                                            .renderImage(page - 1, Math.min(2f, 1800f / longest));
                            terms.addAll(ai.extract(null, imageData(image)));
                            image.flush();
                        }
                    }
                    if (text.length() > 30000)
                        throw invalid("Đoạn PDF quá dài. Hãy chọn ít trang hơn.");
                    if (!text.isEmpty()) terms.addAll(ai.extract(text.toString(), null));
                }
            } else if (magic.startsWith("\u0089PNG")
                    || (bytes.length > 2 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 216)) {
                if (from != 1) throw invalid("Ảnh chỉ có một trang.");
                try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                    var readers = ImageIO.getImageReaders(stream);
                    if (!readers.hasNext()) throw new AppException(ErrorCode.FILE_TYPE_INVALID);
                    var reader = readers.next();
                    try {
                        reader.setInput(stream);
                        if ((long) reader.getWidth(0) * reader.getHeight(0) > 20_000_000)
                            throw invalid("Ảnh tối đa 20 megapixel.");
                        var image = reader.read(0);
                        terms.addAll(ai.extract(null, imageData(image)));
                        image.flush();
                    } finally {
                        reader.dispose();
                    }
                }
            } else if (file.getOriginalFilename() != null
                    && file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                if (from != 1) throw invalid("File văn bản chỉ có một phần.");
                String text =
                        StandardCharsets.UTF_8
                                .newDecoder()
                                .onMalformedInput(CodingErrorAction.REPORT)
                                .decode(ByteBuffer.wrap(bytes))
                                .toString();
                if (text.length() > 30000 || text.indexOf('\0') >= 0)
                    throw invalid("File TXT phải là văn bản UTF-8, tối đa 30.000 ký tự.");
                terms.addAll(ai.extract(text, null));
            } else
                throw new AppException(
                        ErrorCode.FILE_TYPE_INVALID, "Hỗ trợ PDF, ảnh PNG/JPEG và TXT UTF-8.");
            return new Extraction(
                    terms.stream().limit(80).toList(), pages, from, end, terms.size() >= 80);
        } catch (AppException e) {
            throw e;
        } catch (InvalidPasswordException e) {
            throw invalid("PDF có mật khẩu. Hãy mở khóa trước khi nhập.");
        } catch (IOException | IllegalArgumentException e) {
            throw invalid("Không đọc được tài liệu. Hãy kiểm tra file PDF/ảnh/TXT.");
        }
    }

    private String imageData(BufferedImage source) throws IOException {
        int width = source.getWidth(), height = source.getHeight();
        double scale = Math.min(1d, 1800d / Math.max(width, height));
        var resized =
                new BufferedImage(
                        Math.max(1, (int) (width * scale)),
                        Math.max(1, (int) (height * scale)),
                        BufferedImage.TYPE_INT_RGB);
        var graphics = resized.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, resized.getWidth(), resized.getHeight());
        graphics.drawImage(source, 0, 0, resized.getWidth(), resized.getHeight(), null);
        graphics.dispose();
        var out = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", out);
        resized.flush();
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private AppException invalid(String message) {
        return new AppException(ErrorCode.INVALID_CONTENT, message);
    }
}
