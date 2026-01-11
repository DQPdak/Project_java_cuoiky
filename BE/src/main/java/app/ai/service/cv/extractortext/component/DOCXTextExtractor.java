package app.ai.service.cv.extractortext.component;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;
import app.ai.util.TextCleaningUtil;

import java.util.List;

@Component
public class DOCXTextExtractor implements IFileTextExtractor {

    private final TextCleaningUtil textCleaner;

    // Inject Utility làm sạch text
    public DOCXTextExtractor(TextCleaningUtil textCleaner) {
        this.textCleaner = textCleaner;
    }

    // Kiểm tra file có phải định dạng DOCX không
    @Override
    public boolean supports(MultipartFile file) {
         try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Đọc văn bản từ file DOCX
     * * Ý tưởng:
     * 1. Mở tài liệu DOCX sử dụng XWPFDocument
     * 2. Lấy tất cả các đoạn văn trong tài liệu cho vào mảng paragraphs
     * 3. Duyệt qua từng đoạn văn, lấy văn bản và thêm vào StringBuilder text
     * 4. Trả về văn bản thuần túy đã làm sạch (sử dụng TextCleaningUtil)
     */
    @Override
    public String extractText(MultipartFile file) throws Exception {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                if (para.getText() != null && !para.getText().trim().isEmpty()) {
                    text.append(para.getText()).append("\n");
                }
            }
            // Sử dụng Utility chung để làm sạch, thay vì viết hàm riêng
            return textCleaner.clean(text.toString());
        }
    }
}