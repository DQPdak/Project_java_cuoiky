package app.ai.service.cv.extractorexperience.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class CompanyParser {
    
    // Mở rộng từ khóa nhận diện công ty
    private final String[] KEYWORDS = {
        "worked at", "work at", "working at", 
        "làm việc tại", "công tác tại", 
        "company", "công ty", "enterprise", "group"
    };

    public List<String> parse(String text) {
        if (text == null) return new ArrayList<>();
        
        Set<String> results = new HashSet<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            // Bỏ qua dòng quá ngắn (rác OCR)
            if (line.length() < 5) continue;

            for (String kw : KEYWORDS) {
                if (line.toLowerCase().contains(kw)) {
                    String cleanName = extractAndClean(line, kw);
                    if (isValid(cleanName)) {
                        results.add(cleanName);
                    }
                }
            }
        }
        return new ArrayList<>(results);
    }

    private String extractAndClean(String line, String keyword) {
        // Loại bỏ từ khóa (ví dụ bỏ chữ "Worked at")
        String temp = line.replaceAll("(?i)" + Pattern.quote(keyword), "").trim();
        
        // FIX LỖI OCR:
        // Loại bỏ các ký tự đặc biệt ở đầu và cuối chuỗi (ví dụ: "- FPT Software .")
        // Chỉ giữ lại chữ cái, số và khoảng trắng ở giữa
        return temp.replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$", "").trim();
    }

    private boolean isValid(String name) {
        // Tên công ty phải có ít nhất 2 ký tự và không phải toàn số
        return name.length() >= 2 && !name.matches("^\\d+$");
    }
}