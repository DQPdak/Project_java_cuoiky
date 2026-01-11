package app.ai.service.cv.extractorexperience.component;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YearParser {
    
    // Regex tìm năm: 19xx hoặc 20xx (Hỗ trợ cả lỗi OCR 2O21 - chữ O thay vì số 0)
    private static final Pattern YEAR_PATTERN = Pattern.compile("(19\\d{2}|2[0O]\\d{2})");
    
    // Regex tìm từ khóa "Hiện tại"
    private static final Pattern PRESENT_PATTERN = Pattern.compile(
        "(present|current|now|hiện tại|nay)", Pattern.CASE_INSENSITIVE
    );

    /**
     * Tính tổng số năm kinh nghiệm = (Năm lớn nhất - Năm nhỏ nhất)
     */
    public int parse(String text) {
        if (text == null || text.isEmpty()) return 0;
        
        List<Integer> years = new ArrayList<>();

        // 1. Quét toàn bộ văn bản để nhặt ra các năm
        Matcher matcher = YEAR_PATTERN.matcher(text);
        while (matcher.find()) {
            String yearStr = matcher.group();
            // FIX LỖI OCR: Thay thế O (chữ) thành 0 (số)
            yearStr = yearStr.toUpperCase().replace("O", "0");
            
            try {
                years.add(Integer.parseInt(yearStr));
            } catch (NumberFormatException ignored) {}
        }

        // 2. Nếu tìm thấy từ khóa "Present", thêm năm hiện tại vào danh sách
        Matcher presentMatcher = PRESENT_PATTERN.matcher(text);
        if (presentMatcher.find()) {
            years.add(Year.now().getValue());
        }

        if (years.isEmpty()) return 0;

        // 3. Tính toán khoảng cách (Max - Min)
        int minYear = Collections.min(years);
        int maxYear = Collections.max(years);

        // Lọc nhiễu: Nếu năm min quá nhỏ (do OCR sai), lấy năm max
        // Ví dụ OCR đọc nhầm số điện thoại thành năm
        if (minYear < 1980) return 1; 

        return Math.max(1, maxYear - minYear);
    }
}