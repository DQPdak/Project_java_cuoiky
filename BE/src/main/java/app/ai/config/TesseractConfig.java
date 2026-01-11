package app.ai.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * CHỨC NĂNG:
 * - Chịu trách nhiệm khởi tạo và cấu hình đối tượng Tesseract.
 * - Đảm bảo rằng logic cấu hình đường dẫn dữ liệu (tessdata) được tách biệt khỏi logic xử lý nghiệp vụ.
 */
@Configuration
public class TesseractConfig {

    @Bean
    public Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();
        
        // Cấu hình đường dẫn đến thư mục chứa dữ liệu training (tessdata)
        // Lưu ý: Đường dẫn này đang trỏ vào resources/tessdata của project
        File tessDataFolder = new File("src/main/resources/tessdata");
        
        // Set đường dẫn tuyệt đối để Tesseract tìm thấy file .traineddata
        tesseract.setDatapath(tessDataFolder.getAbsolutePath());
        
        // Cấu hình ngôn ngữ: Ưu tiên Tiếng Việt, sau đó là Tiếng Anh
        tesseract.setLanguage("vie+eng");
        
        return tesseract;
    }
}