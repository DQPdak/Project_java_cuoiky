package app.ai.service.cv.extractortext.component;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;
import java.util.List;

@Component
public class DOCXTextExtractor implements IFileTextExtractor {

    @Override
    public boolean supports(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && 
               (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc"));
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph para : paragraphs) {
                // Chỉ lấy các đoạn có chữ
                String paraText = para.getText();
                if (paraText != null && !paraText.trim().isEmpty()) {
                    // Thêm xuống dòng thủ công vì DOCX tách theo Paragraph object
                    text.append(paraText).append("\n");
                }
            }
            return cleanForAI(text.toString());
        }
    }

    private String cleanForAI(String text) {
        if (text == null) return "";
        return text.replaceAll("[\\t\\u00A0]+", " ")
                   .replaceAll("\\n\\s*\\n", "\n\n")
                   .replaceAll("(?m)^\\s+|\\s+$", "") // Xóa space thừa đầu dòng
                   .trim();
    }
}