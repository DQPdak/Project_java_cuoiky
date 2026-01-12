package app.ai.service.cv.extractortext;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CVTextExtractor {

    public String extractText(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return "";
        
        try {
            if (fileName.toLowerCase().endsWith(".pdf")) {
                // Đọc PDF
                try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                    return new PDFTextStripper().getText(document);
                }
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                // Đọc Word
                try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
                    StringBuilder text = new StringBuilder();
                    for (XWPFParagraph para : document.getParagraphs()) {
                        text.append(para.getText()).append("\n");
                    }
                    return text.toString();
                }
            }
            return "";
        } catch (Exception e) {
            log.error("Lỗi đọc file: ", e);
            return "";
        }
    }
}