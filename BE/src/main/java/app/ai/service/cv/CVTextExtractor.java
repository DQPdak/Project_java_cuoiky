package app.ai.service.cv;

/**
 * CHỨC NĂNG: 
 * + Trích xuất văn bản từ CV
 * + Hỗ trợ đọc file CV định dạng PDF, DOCX sau đó chuyển đổi nội dung thành chuỗi văn bản thuần túy.
 * 
 * SỬ DỤNG THƯ VIỆN:
 * + Apache PDFBox: Để trích xuất văn bản từ file PDF.
 * + Apache POI: Để trích xuất văn bản từ file DOCX.
 */

// import từ thư viện Apache PDFBox
import org.apache.pdfbox.pdmodel.PDDocument; // dùng để mở, tạo, chỉnh sửa hoặc lưu tài liệu PDF
import org.apache.pdfbox.Loader; // dùng để tải tài liệu PDF từ InputStream
import org.apache.pdfbox.text.PDFTextStripper; // dùng để trích xuất văn bản từ PDF

// import từ thư viện Apache POI
import org.apache.poi.xwpf.usermodel.XWPFDocument; // dùng để đọc hoặc tạo tài liệu DOCX
import org.apache.poi.xwpf.usermodel.XWPFParagraph; // dùng để truy xuất chỉnh sửa đoạn văn trong DOCX

// import từ Spring Framework
import org.springframework.stereotype.Service; // Annotation đánh dấu lớp Service trong Spring
import org.springframework.web.multipart.MultipartFile; // Đại diện cho file được upload, dùng để nhận file upload từ client

// import từ java Core
import java.io.InputStream;// Dòng dữ liệu đầu vào dùng để đọc file hoặc dữ liệu từ mạng
import java.util.List;// danh sách các phần tử, thường dùng để lưu trữ nhiều đoạn văn, dòng dữ liệu

@Service
public class CVTextExtractor {
    private static final int Max_Page_Limit = 5; // Giới hạn số trang tối đa để trích xuất từ file PDF

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

            if(isPDF(file)){
                return extractFromPDF(file);
            }
            else if(isDOCX(file)){
                return extractFromDOCX(file.getInputStream());
            }
            else{
                throw new IllegalArgumentException("Không xác định được loại file: " + file.getOriginalFilename());
            }

        } catch (Exception e){
            throw new RuntimeException("Lỗi tách văn bản từ CV: " + e.getMessage(), e);
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
    private String extractFromPDF(MultipartFile inputText) throws Exception {
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
     * Đọc văn bản từ file DOCX
     * 
     * Ý tưởng:
     * 1. Mở tài liệu DOCX sử dụng XWPFDocument
     * 2. Lấy tất cả các đoạn văn trong tài liệu cho vào mảng paragraphs
     * 3. Duyệt qua từng đoạn văn, lấy văn bản và thêm vào StringBuilder text
     * 4. Trả về văn bản thuần túy đã làm sạch
     */
    private String extractFromDOCX(InputStream inputText) throws Exception {
        try(XWPFDocument document = new XWPFDocument(inputText)){
            // StringBuilder cũng sử lý văn bản như string nhưng khi thây đổi văn bạn sẽ không tạo đối tượng mới như string
            StringBuilder text = new StringBuilder();
            
        //    Lấy tất cả các đoạn văn trong tài liệu
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            // Duyệt qua từng đoạn văn và thêm vào StringBuilder
            for(XWPFParagraph para : paragraphs){
                String paraText = para.getText();

                // Chỉ thêm đoạn văn không rỗng
                if(paraText != null && !paraText.trim().isEmpty()){
                    text.append(paraText).append("\n");
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
    private String cleanText(String text){
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

    // Kiểm tra file có phải định dạng PDF không
    private boolean isPDF(MultipartFile file){
    try {
        byte[] fileBytes = file.getBytes(); // chuyển MultipartFile thành byte[]
        try (PDDocument doc = Loader.loadPDF(fileBytes)) {
            return true;
        }
    } catch (Exception e) {
        return false;
    }
}

    // Kiểm tra file có phải định dạng DOCX không
    private boolean isDOCX(MultipartFile file){
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
