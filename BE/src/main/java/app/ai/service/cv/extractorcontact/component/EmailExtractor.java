package app.ai.service.cv.extractorcontact.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import app.ai.service.cv.extractorcontact.Interface.IContactDetailExtractor;

/**
 * CHỨC NĂNG: Trích xuất email từ văn bản CV
 * Nâng cấp: Xử lý nhiễu OCR
 */
@Component
public class EmailExtractor implements IContactDetailExtractor {

    // Regex tìm email: Linh hoạt hơn, không dùng ^ và $ để có thể tìm thấy trong đoạn văn dài
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}", 
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String extract(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        // BƯỚC 1: Sơ cứu dữ liệu OCR (OCR Sanitization)
        // OCR hay đọc nhầm: "name @ gmail . com" -> Cần xóa khoảng trắng quanh @ và .
        // Fix lỗi phổ biến: gmai1 -> gmail
        String cleanText = text
                .replaceAll("\\s*@\\s*", "@")   // Xóa space quanh @
                .replaceAll("\\s*\\.\\s*", ".") // Xóa space quanh .
                .replaceAll("(?i)gmai1", "gmail"); 

        // BƯỚC 2: Trích xuất bằng Regex chuẩn
        Matcher matcher = EMAIL_PATTERN.matcher(cleanText);
        
        // Chỉ lấy email đầu tiên tìm thấy
        if (matcher.find()) {
            return matcher.group().toLowerCase();
        }
        
        return null; // Không tìm thấy
    }
}