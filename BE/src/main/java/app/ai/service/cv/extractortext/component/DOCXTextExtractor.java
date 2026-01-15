package app.ai.service.cv.extractortext.component;

import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class DOCXTextExtractor implements IFileTextExtractor {

    // --- HỖ TRỢ MULTIPART FILE (Upload Form) ---
    @Override
    public boolean supports(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null &&
               (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc"));
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            return processDocument(document);
        }
    }

    // --- [MỚI] HỖ TRỢ FILE (Tải từ URL/Cloudinary) ---
    @Override
    public boolean supports(File file) {
        String fileName = file.getName();
        return fileName != null &&
               (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc"));
    }

    @Override
    public String extractText(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            return processDocument(document);
        }
    }

    // --- LOGIC TRÍCH XUẤT CHUNG (Tách ra để tái sử dụng) ---
    private String processDocument(XWPFDocument document) {
        StringBuilder text = new StringBuilder();
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        for (XWPFParagraph para : paragraphs) {
            String paraText = para.getText();
            // Chỉ lấy các đoạn có chữ
            if (paraText != null && !paraText.trim().isEmpty()) {
                // Thêm xuống dòng thủ công vì DOCX tách theo Paragraph object
                text.append(paraText).append("\n");
            }
        }
        return cleanForAI(text.toString());
    }

    // Hàm làm sạch dữ liệu cho AI
    private String cleanForAI(String text) {
        if (text == null) return "";
        return text.replaceAll("[\\t\\u00A0]+", " ")  // Thay tab/nbsp bằng space
                   .replaceAll("\\n\\s*\\n", "\n\n")  // Gộp dòng trống
                   .replaceAll("(?m)^\\s+|\\s+$", "") // Xóa space thừa đầu/cuối dòng
                   .trim();
    }
}