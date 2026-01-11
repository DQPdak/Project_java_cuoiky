package app.ai.service.cv.extractortext.component;

import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;
import app.ai.util.FileConversionUtil;
import app.ai.util.TextCleaningUtil;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * CHỨC NĂNG:
 * - Trích xuất văn bản từ các file hình ảnh (JPG, PNG, JPEG).
 * - Sử dụng thư viện Tesseract OCR thông qua cấu hình đã được inject.
 * * NGUYÊN TẮC SOLID:
 * - Dependency Inversion: Class này không tự tạo Tesseract hay FileUtil mà nhận qua Constructor.
 */
@Component
public class ImageTextExtractor implements IFileTextExtractor {

    private final Tesseract tesseract;
    private final FileConversionUtil fileUtil;
    private final TextCleaningUtil textCleaner;

    // Constructor Injection: Spring tự động tiêm các dependencies vào đây
    public ImageTextExtractor(Tesseract tesseract, FileConversionUtil fileUtil, TextCleaningUtil textCleaner) {
        this.tesseract = tesseract;
        this.fileUtil = fileUtil;
        this.textCleaner = textCleaner;
    }

    /**
     * Kiểm tra xem file upload có phải là ảnh hay không.
     */
    @Override
    public boolean supports(MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        return ext != null && (ext.equalsIgnoreCase("jpg") 
                            || ext.equalsIgnoreCase("jpeg") 
                            || ext.equalsIgnoreCase("png"));
    }

    /**
     * Quy trình xử lý:
     * 1. Chuyển MultipartFile -> File tạm (nhờ FileUtil).
     * 2. Gọi Tesseract đọc text từ File tạm.
     * 3. Xóa File tạm.
     * 4. Làm sạch text (nhờ TextCleaningUtil) và trả về.
     */
    @Override
    public String extractText(MultipartFile file) throws Exception {
        File tempFile = null;
        try {
            // Bước 1: Chuyển đổi file
            tempFile = fileUtil.convert(file);
            
            // Bước 2: OCR
            String rawText = tesseract.doOCR(tempFile);
            
            // Bước 3: Làm sạch và trả về
            return textCleaner.clean(rawText);
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi OCR hình ảnh: " + e.getMessage(), e);
        } finally {
            // Luôn đảm bảo xóa file tạm để không rác ổ cứng
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}