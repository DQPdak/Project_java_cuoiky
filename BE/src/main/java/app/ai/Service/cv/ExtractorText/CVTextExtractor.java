package app.ai.service.cv.extractorText;

/**
 * CHỨC NĂNG: 
 * + Trích xuất văn bản từ CV
 * + Hỗ trợ đọc file CV định dạng PDF, DOCX sau đó chuyển đổi nội dung thành chuỗi văn bản thuần túy.
 * 
 */

// import từ Spring Framework
import org.springframework.stereotype.Service; // Annotation đánh dấu lớp Service trong Spring
import org.springframework.web.multipart.MultipartFile; // Đại diện cho file được upload, dùng để nhận file upload từ client

import app.ai.service.cv.extractorText.component.DOCXTextExtractor;
import app.ai.service.cv.extractorText.component.PDFTextExtractor;
import app.ai.service.cv.extractorText.Interface.IFileTextExtractor;


@Service
public class CVTextExtractor {
    private IFileTextExtractor pdf= new PDFTextExtractor();
    private IFileTextExtractor docx= new DOCXTextExtractor();
    /**
     * Trích xuất văn bản từ file CV (PDF hoặc DOCX)
     * 
     * Ý tưởng:
     * 1. kiểm tra loại file (PDF hoặc DOCX)
     * 2. nếu là PDF, sử dụng Apache PDFBox để trích xuất văn bản
     * 3. nếu là DOCX, sử dụng Apache POI để trích xuất văn bản
     * 4, Clean và trả về văn bản thuần túy
     */
    public String extractText(MultipartFile file){
        try{

            if(pdf.supports(file)){
                return pdf.extractText(file);
            }
            else if(docx.supports(file)){
                return docx.extractText(file);
            }
            else{
                throw new IllegalArgumentException("Không xác định được loại file: " + file.getOriginalFilename());
            }

        } catch (Exception e){
            throw new RuntimeException("Lỗi tách văn bản từ CV: " + e.getMessage(), e);
        }
    }
}
