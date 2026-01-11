package app.ai.service.cv.extractorcontact.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import app.ai.service.cv.extractorcontact.Interface.IContactDetailExtractor;

@Component
public class PhoneExtractor implements IContactDetailExtractor {

    // Regex hỗ trợ: +84 hoặc 0, theo sau là các đầu số nhà mạng phổ biến
    // Chấp nhận các ký tự ngăn cách như dấu chấm, gạch ngang, khoảng trắng
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(\\+84|0)(3|5|7|8|9|1[2|6|8|9])([0-9\\s.\\-]{7,13})"
    );

    @Override
    public String extract(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        // Chiến thuật: Chia nhỏ văn bản thành từng dòng để xử lý chính xác hơn
        String[] lines = text.split("\n");
        
        for (String line : lines) {
            // Bỏ qua các dòng quá ngắn hoặc không chứa số (tối ưu hiệu năng)
            if (line.length() < 8 || !line.matches(".*\\d.*")) continue;

            // BƯỚC 1: Fix lỗi OCR điển hình (Chữ 'O' hoặc 'o' bị đọc nhầm từ số '0')
            // Logic: Thay thế O/o thành 0 nếu nó nằm cạnh một con số
            String normalizedLine = line.replaceAll("(?<=\\d)[Oo](?=\\d)|(?<=\\d)[Oo]|(?<![a-zA-Z])[Oo](?=\\d)", "0");
            
            // BƯỚC 2: Matching
            Matcher matcher = PHONE_PATTERN.matcher(normalizedLine);
            if (matcher.find()) {
                // Lấy kết quả thô
                String rawPhone = matcher.group();
                
                // BƯỚC 3: Làm sạch lần cuối (chỉ giữ lại số và dấu +)
                return cleanPhoneNumber(rawPhone);
            }
        }
        return null;
    }

    private String cleanPhoneNumber(String rawPhone) {
        // Xóa tất cả ký tự không phải số, trừ dấu + ở đầu
        return rawPhone.replaceAll("[^0-9+]", "");
    }
}