package app.ai.Service.cv.ExtractorContact.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import app.ai.Service.cv.Interfaces.IContactDetailExtractor;

/**
 * CHỨC NĂNG: Trích xuất email từ văn bản CV
 */
@Component
public class EmailExtractor implements IContactDetailExtractor {
    // Biểu diễn chính quy để tìm email

    /**
     * Cấu trúc email: username@domain.extension
     * 
     * - Username: Chứa chữ cái, số, dấu chấm (.), dấu gạch dưới (_), dấu gạch ngang (-) -> [a-zA-Z0-9._%+-]
     * ký tự @ -> @
     * - Domain: Chứa chữ cái, số, dấu gạch ngang (-), dấu chấm (.) -> [a-zA-Z0-9.-]
     * - Extension: Chứa chữ cái, thường có độ dài ít nhất 2 ký tự -> [a-zA-Z]{2,}
     * - Có thể có nhiều phần mở rộng (ví dụ .co.uk) -> (\\.[a-zA-Z]{2,})+
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+(\\.[a-zA-Z]{2,})+$"
    );
    @Override
    public String extract(String text) {
        if (text == null || text.isEmpty()){
            return null;
        }

        // Tạo đối tượng Matcher từ regex EMAIL_PATTERN và chuỗi text
        Matcher matcher = EMAIL_PATTERN.matcher(text);

        // Tìm đoạn đầu tiên trong text khớp với regex
        if (matcher.find()){
            String email = matcher.group(); // Lấy chuỗi con khớp với regex (ví dụ email tìm được)

            // Kiểm tra tính hợp lệ của email
            if (isValidEmail(email)){
                return email.toLowerCase(); // Trả về email ở dạng chữ thường để dễ so sánh, lưu trữ
            }
        }
        return null; // Trả về null nếu không tìm thấy email
    }

    // kiểm tra tính hợp lệ của email
    /**
     * Nguyên tắc kiểm tra:
     * - Email phải có đúng một ký tự @
     * - Phần trước @ phải chứa ít nhất một ký tự hợp lệ
     * - Phần sau @ phải chứa ít nhất một ký tự hợp lệ và có ít nhất một dấu chấm (.)
     * - Phần mở rộng sau dấu chấm phải có ít nhất 2 ký tự
     */
    private boolean isValidEmail(String email) {
        // Kiểm tra null hoặc rỗng
        if (email == null || email.isEmpty()) {
            return false;
        }
        // kiểm tra theo định dạng regex
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+(\\.[a-zA-Z]{2,})+$");
    }
}
