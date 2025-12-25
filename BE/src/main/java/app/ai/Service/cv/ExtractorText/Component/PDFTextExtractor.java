package app.ai.Service.cv.ExtractorText.Component;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import app.ai.Service.cv.Interfaces.IFileTextExtractor;

@Component
public class PDFTextExtractor implements IFileTextExtractor {
    private static final int Max_Page_Limit = 5; // Giới hạn số trang tối đa để trích xuất từ file PDF

    // Kiểm tra file có phải định dạng PDF không
    @Override
    public boolean supports(MultipartFile file) {
        try {
        byte[] fileBytes = file.getBytes(); // chuyển MultipartFile thành byte[]
        try (PDDocument doc = Loader.loadPDF(fileBytes)) {
            return true;
        }
    } catch (Exception e) {
        return false;
    }
    }

    /**
     * Đọc văn bản từ file PDF
     * 
     * Ý tưởng:
     * 1. Chuyển tài liệu MultipartFile thành byte[]
     * 2. Mở tài liệu PDF sử dụng Loader từ byte[]
     * 3. Sử dụng PDFTextStripper để trích xuất văn bản
     * 4. Giới hạn số trang tối đa để trích xuất là 5 trang
     * 5. Trả về văn bản thuần túy đã làm sạch
     */
    @Override
    public String extractText(MultipartFile inputText) throws Exception {
        try{
           byte[] fileBytes = inputText.getBytes(); // chuyển MultipartFile thành byte[]
            try(PDDocument document = Loader.loadPDF(fileBytes)){
            // PDFTextStripper để trích xuất văn bản từ PDF
            PDFTextStripper stripper = new PDFTextStripper();
            
            // Giới hạn số trang tối đa để trích xuất là 5 trang
            int pageCount = Math.min(document.getNumberOfPages(), Max_Page_Limit);
            stripper.setEndPage(pageCount);

            // Trích xuất văn bản
            String rawText = stripper.getText(document);

            // làm sạch và trả về văn bản thuần túy
            return cleanText(rawText);
        }
        } catch (Exception e){
            throw new RuntimeException("Lỗi trích xuất văn bản từ PDF: " + e.getMessage(), e);
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
