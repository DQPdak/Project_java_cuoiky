package app.ai.service.cv.extractortext.component;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;

import java.util.List;

@Component
public class DOCXTextExtractor implements IFileTextExtractor {

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
     * 
     * Ý tưởng:
     * 1. Mở tài liệu DOCX sử dụng XWPFDocument
     * 2. Lấy tất cả các đoạn văn trong tài liệu cho vào mảng paragraphs
     * 3. Duyệt qua từng đoạn văn, lấy văn bản và thêm vào StringBuilder text
     * 4. Trả về văn bản thuần túy đã làm sạch
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
            return cleanText(text.toString());
        }
    }

    /**
     * Làm sạch văn bản thuần túy
     * + Loại bỏ nhiều space liên tiếp thành một space
     * + Xóa các ký tự đặc biệt không cần thiết
     * + Chuẩn hóa ký tự xuống dòng
     * + Xóa bỏ khoảng trắng thừa ở đầu cuối
     */
    private String cleanText(String text) {
          if (text == null) {
            return "";
        }

        return text
            //Xóa nhiều space thành 1 space
            .replaceAll("\\s+", " ")
            // Xóa ký tự đặc biệt 
            .replaceAll("[^\\p{L}\\p{N}\\s@.+\\-():/,]", "")
            // Chuẩn hóa ký tự xuống dòng
            .replaceAll("\n{3,}", "\n\n")
            // Xóa khoảng trắng thừa ở đầu cuối
            .trim();
    }

}
