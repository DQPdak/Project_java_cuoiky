package app.ai.service.cv.dto;

import app.ai.service.cv.extractorcontact.dto.ContactInfo;
import app.ai.service.cv.extractorexperience.dto.ExperienceDTO;
import app.ai.service.cv.skill.component.recomment.dto.ComprehensiveRecommendation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVAnalysisResult {
    // 1. Thông tin Liên lạc
    private ContactInfo contactInfo;

    // 2. Thông tin kinh nghiệm
    private ExperienceDTO experience;

    // 3. Thông tin tương thích với công việc
    // "Mức độ phù hợp" lấy từ Map của SkillService
    private Object matchScore; 
    
    // "Gợi ý bổ sung"
    private ComprehensiveRecommendation recommendations; 

    // 4. Dữ liệu thô và thời gian làm việc (để đo KPI < 5s)
    private String rawText;
    private long processingTimeMs;
}