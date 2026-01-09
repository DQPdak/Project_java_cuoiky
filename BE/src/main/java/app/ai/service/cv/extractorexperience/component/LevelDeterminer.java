package app.ai.service.cv.extractorexperience.component;

import org.springframework.stereotype.Component;

@Component
public class LevelDeterminer {
    private final String[] SENIOR_KW = {"senior", "lead", "principal", "architect", "trưởng nhóm"};
    private final String[] JUNIOR_KW = {"junior", "intern", "fresher", "trainee", "thực tập"};

    public String determine(int years, String text) {
        String lowerText = text.toLowerCase();
        
        // Quy tắc Senior: Có keyword Senior HOẶC >= 5 năm kinh nghiệm
        if (containsAny(lowerText, SENIOR_KW) || years >= 5) return "SENIOR";
        
        // Quy tắc Junior: Có keyword Junior HOẶC <= 2 năm kinh nghiệm
        if (containsAny(lowerText, JUNIOR_KW) || years <= 2) return "JUNIOR";
        
        return "MIDDLE";
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) { if (text.contains(kw)) return true; }
        return false;
    }
}
