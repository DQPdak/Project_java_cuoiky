package app.ai.service.cv.extractorcontact.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import app.ai.service.cv.extractorcontact.Interface.IContactDetailExtractor;

@Component
public class PhoneExtractor implements IContactDetailExtractor{
    // Biểu diễn chính quy để tìm số điện thoại

    /**
     * Cấu trúc thường thấy của số điện thoại:
     * - Có thể bắt đầu với mã quốc gia (+84 hoặc 0) -> (?:\\+84|0)
     * - Tiếp đến là 1 cập số phải là 3x, 5x, 7x, 8x, hoặc 9x -> (?:3\\d|5\\d|7\\d|8\\d|9\\d)
     * - Có thể có dấu cách hoặc dấu gạch ngang giữa các nhóm số -> [\\s-]?
     * 
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(?:\\+84|0)(?:3\\d|5\\d|7\\d|8\\d|9\\d)[\\s-]?\\d{3}[\\s-]?\\d{3,4}$"  
    );

    @Override
    public String extract(String text) {
        if (text == null || text.isEmpty()){
            return null;
        }

        // Tạo đối tượng Matcher từ regex PHONE_PATTERN và chuỗi text
        Matcher matcher = PHONE_PATTERN.matcher(text);

        // Tìm đoạn đầu tiên trong text khớp với regex
        if (matcher.find()){
            String phoneNumber = matcher.group(); // Lấy chuỗi con khớp với regex (ví dụ số điện thoại tìm được)

            // Chuẩn hóa định dạng số điện thoại
            phoneNumber = phoneNumber.replaceAll("[\\s-]", ""); // Xóa dấu cách, gạch ngang

            // kiểm tra tính hợp lệ của số điện thoại
            if (isValidPhoneNumber(phoneNumber)){
                return phoneNumber;
            }
        }
        return null; // Trả về null nếu không tìm thấy số điện thoại
    }

    // kiểm tra tính hợp lệ của số điện thoại
    /**
     * Nguyên tắc kiểm tra:
     * - Số điện thoại phải có độ dài 10 chữ số
     * - Có thể bắt đầu bằng mã quốc gia +84 hoặc 0
     * - Các chữ số còn lại phải là số từ 0-9
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        // Kiểm tra độ dài và định dạng số điện thoại
        return phoneNumber.matches("^(\\+84|0)(?:3\\d|5\\d|7\\d|8\\d|9\\d)\\d{7}$");
    }
}
