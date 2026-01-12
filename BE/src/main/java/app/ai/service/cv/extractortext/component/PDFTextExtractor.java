package app.ai.service.cv.extractortext.component;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;
import java.io.IOException;

@Component
public class PDFTextExtractor implements IFileTextExtractor {
    
    // Giới hạn trang để tránh AI bị quá tải token nếu CV quá dài (thường CV chỉ 1-3 trang)
    private static final int MAX_PAGE_LIMIT = 5;

    @Override
    public boolean supports(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        try {
            // Sử dụng Loader.loadPDF (Dành cho PDFBox 3.0.x)
            // Sửa lại đoạn đọc file PDF (Dành cho bản 3.0)
            try (PDDocument document = Loader.loadPDF(file.getBytes())) { // <--- Dùng Loader.loadPDF thay vì PDDocument.load
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true); // Để nó không đọc lộn xộn cột trái/phải
                
                String rawText = stripper.getText(document);
                return cleanForAI(rawText);
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file PDF: " + e.getMessage(), e);
        }
    }

    // Hàm làm sạch dành riêng cho AI (Không xóa cấu trúc)
    private String cleanForAI(String text) {
        if (text == null) return "";
        
        return text
            // 1. Thay thế các loại khoảng trắng lạ (tab, non-breaking space) thành space thường
            // Nhưng KHÔNG được xóa \n (xuống dòng)
            .replaceAll("[\\t\\u00A0]+", " ")
            
            // 2. Xóa các dòng trống dư thừa (nếu có 3 dòng trống liên tiếp -> gộp thành 2)
            // Để tách rõ các đoạn văn (Paragraphs) cho AI dễ nhận diện
            .replaceAll("\\n\\s*\\n", "\n\n")
            
            // 3. Xóa khoảng trắng thừa ở đầu/cuối mỗi dòng
            .replaceAll("(?m)^\\s+|\\s+$", "")
            
            .trim();
    }
}