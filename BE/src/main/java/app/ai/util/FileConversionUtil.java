package app.ai.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * CHỨC NĂNG:
 * - Chịu trách nhiệm chuyển đổi các đối tượng MultipartFile (trên RAM) thành File vật lý (trên ổ cứng tạm).
 * - Tự động tạo tên file tạm để tránh trùng lặp.
 */
@Component
public class FileConversionUtil {

    public File convert(MultipartFile file) throws IOException {
        // Tạo file tạm với prefix là "ocr_" và giữ nguyên đuôi mở rộng gốc
        File convFile = File.createTempFile("ocr_", "-" + Objects.requireNonNull(file.getOriginalFilename()));
        
        // Ghi dữ liệu từ MultipartFile vào file tạm
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        
        return convFile;
    }
}