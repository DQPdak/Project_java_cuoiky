package app.ai.service.cv.skill.component;

import org.springframework.stereotype.Component;

@Component
public class SkillTextNormalizer {

    /**
     * Sửa nhanh các lỗi OCR phổ biến cho các từ khóa IT quan trọng.
     * Hàm này chạy TRƯỚC khi logic trích xuất bắt đầu.
     */
    public String normalize(String text) {
        if (text == null) return "";
        
        return text
            // 1. Fix lỗi Java (phổ biến nhất)
            .replaceAll("(?i)j@va", "Java")
            .replaceAll("(?i)jav4", "Java")
            
            // 2. Fix lỗi C# / C++ (OCR hay tách rời dấu)
            .replaceAll("(?i)c\\s*#", "C#")      // Ví dụ: "C #" -> "C#"
            .replaceAll("(?i)c\\s*\\+\\+", "C++") // Ví dụ: "C ++" -> "C++"
            
            // 3. Fix lỗi .NET (OCR hay đọc dấu chấm thành dấu khác hoặc tách rời)
            .replaceAll("(?i)dot\\s*net", ".NET")
            .replaceAll("(?i)\\.\\s*net", ".NET")
            
            // 4. Fix lỗi Python (số 0 thay vì chữ o)
            .replaceAll("(?i)pyth0n", "Python")
            
            // 5. Fix lỗi SQL (số 0, số 1)
            .replaceAll("(?i)s0l", "SQL")
            .replaceAll("(?i)sq1", "SQL")

            // 6. Xóa các ký tự nhiễu lạ lùng (Non-ASCII) do OCR sinh ra
            // Chỉ giữ lại chữ cái, số, và các dấu câu cơ bản
            .replaceAll("[^\\x00-\\x7F]+", " "); 
    }
}