package app.ai.service.cv;

/**
 * CHỨC NĂNG: Trích xuất thông tin liên hệ từ văn bản CV(Email, SĐT)
 * 
 * SỬ DỤNG THƯ VIỆN: thư viện Regex cho từng loại thông tin
 * 
 */

// import từ Spring Framework
import org.springframework.stereotype.Service; // Annotation đánh dấu lớp Service trong Spring

// import từ java Core
import java.util.regex.Pattern; // Lớp đại diện cho biểu thức chính quy đã biểu diễn
import java.util.regex.Matcher; // Lớp dùng để thực hiện các thao tác tìm kiếm

// import từ dự án
import app.ai.dto.ContactInfo; // Lớp DTO để lưu trữ thông tin liên hệ

@Service
public class ContactInfoExtractor {

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
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+(\\.[a-zA-Z]{2,})+$"
    );

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

    /**
     * Phương thức trích xuất thông tin liên hệ (Email, SĐT) từ văn bản CV
     */
    public ContactInfo extract (String cvText){
        ContactInfo info = new ContactInfo();

        // trich xuất email
        String email = extractEmail(cvText);
        info.setEmail(email);

        // trích xuất số điện thoại
        String phoneNumber = extractPhoneNumber(cvText);
        info.setPhoneNumber(phoneNumber);

        return info;
    }

    // Phương thức trích xuất email từ văn bản CV
    /**
     * Ý tưởng:
     * - Dùng regex để tìm kiếm mẫu email trong văn bản
     * - Trả về email đầu tiên tìm thấy hoặc null nếu không tìm thấy
     * - Kiểm tra email có hợp lệ không trước khi trả về
     */
    public String extractEmail (String text){
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
        return null; // Trả về null nếu không tìm thấy email hợp lệ
    }

    // Phương thức trích xuất số điện thoại từ văn bản CV
    /**
     * Ý tưởng:
     * - Dùng regex để tìm kiếm mẫu số điện thoại trong văn bản
     * - Trả về số điện thoại đầu tiên tìm thấy hoặc null nếu không tìm thấy
     * - Chuẩn hóa định dạng số điện thoại sang liên tiếp không dấu cách, gạch ngang
     */
    public String extractPhoneNumber (String text){
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

    // Lớp nội bộ để lưu trữ thông tin liên hệ
    
}