package app.ai.service.cv.extractorexperience.component;

import org.springframework.stereotype.Component;

@Component
public class LevelDeterminer {
    // Bổ sung thêm các chức danh quản lý
    private final String[] SENIOR_KW = {
        "senior", "lead", "principal", "architect", "manager", "head of", "trưởng nhóm", "quản lý"
    };
    
    private final String[] JUNIOR_KW = {
        "junior", "intern", "fresher", "trainee", "apprentice", "thực tập", "nhân viên mới"
    };

    public String determine(int years, String text) {
        if (text == null) text = "";
        String lowerText = text.toLowerCase();
        
        // Ưu tiên 1: Check số năm kinh nghiệm (Chính xác hơn keywords)
        if (years >= 5) return "SENIOR";
        if (years <= 2) return "JUNIOR";

        // Ưu tiên 2: Check keywords nếu số năm lửng lơ (3-4 năm) hoặc parser năm bị lỗi
        if (containsAny(lowerText, SENIOR_KW)) return "SENIOR";
        if (containsAny(lowerText, JUNIOR_KW)) return "JUNIOR";
        
        // Mặc định là Middle
        return "MIDDLE";
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}