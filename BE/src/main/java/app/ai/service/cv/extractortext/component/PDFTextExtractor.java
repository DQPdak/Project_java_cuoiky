package app.ai.service.cv.extractortext.component;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;
import java.io.File;
import java.io.IOException;

@Component
public class PDFTextExtractor implements IFileTextExtractor {

    @Override
    public boolean supports(MultipartFile file) {
        return file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".pdf");
    }

    @Override
    public boolean supports(File file) { // [MỚI]
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return processDoc(document);
        }
    }

    @Override
    public String extractText(File file) throws Exception { // [MỚI]
        try (PDDocument document = Loader.loadPDF(file)) {
            return processDoc(document);
        }
    }

    // Tách logic chung ra để tái sử dụng
    private String processDoc(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        return cleanForAI(stripper.getText(document));
    }

    private String cleanForAI(String text) {
        if (text == null) return "";
        return text.replaceAll("[\\t\\u00A0]+", " ").replaceAll("\\n\\s*\\n", "\n\n").trim();
    }
}