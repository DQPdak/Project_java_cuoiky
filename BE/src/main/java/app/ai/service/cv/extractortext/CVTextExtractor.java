package app.ai.service.cv.extractortext;

/**
 * CHỨC NĂNG: 
 * + Trích xuất văn bản từ CV
 * + Hỗ trợ đọc file CV định dạng PDF, DOCX, và Hình ảnh (JPG, PNG) sau đó chuyển đổi nội dung thành chuỗi văn bản thuần túy.
 * */

// import từ Spring Framework
import org.springframework.stereotype.Service; // Annotation đánh dấu lớp Service trong Spring
import org.springframework.web.multipart.MultipartFile; // Đại diện cho file được upload, dùng để nhận file upload từ client

import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;

import java.util.List;

@Service
public class CVTextExtractor {
    
    // Thay vì khai báo cứng từng biến, ta dùng List để Spring tự động gom tất cả các Bean implement IFileTextExtractor
    // (Bao gồm: PDFTextExtractor, DOCXTextExtractor, và ImageTextExtractor mới thêm)
    private final List<IFileTextExtractor> extractors;

    // Constructor Injection: Spring sẽ tự động "tiêm" danh sách vào đây
    public CVTextExtractor(List<IFileTextExtractor> extractors) {
        this.extractors = extractors;
    }

    /**
     * Trích xuất văn bản từ file CV
     * * Ý tưởng (Cập nhật):
     * 1. Duyệt qua danh sách các bộ trích xuất (PDF, DOCX, Image...) có trong hệ thống
     * 2. Hỏi từng bộ xem có hỗ trợ file này không (dựa vào hàm supports)
     * 3. Nếu bộ nào gật đầu (true) -> Gọi hàm extractText để lấy nội dung ngay lập tức
     * 4. Nếu đi hết danh sách mà không ai nhận -> Báo lỗi
     */
    public String extractText(MultipartFile file){
        try{
            // Duyệt qua danh sách các "thợ lặn" (extractors)
            for (IFileTextExtractor extractor : extractors) {
                // Hỏi: "Bạn có xử lý được file này không?"
                if (extractor.supports(file)) {
                    // Nếu được, giao việc luôn
                    return extractor.extractText(file);
                }
            }
            
            // Nếu không ai nhận xử lý
            throw new IllegalArgumentException("Không xác định được loại file hoặc định dạng chưa hỗ trợ: " + file.getOriginalFilename());

        } catch (Exception e){
            throw new RuntimeException("Lỗi tách văn bản từ CV: " + e.getMessage(), e);
        }
    }
}