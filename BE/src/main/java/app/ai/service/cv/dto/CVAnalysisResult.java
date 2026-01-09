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
    private Object matchScore; // "Mức độ phù hợp" từ scoreData
    private ComprehensiveRecommendation recommendations; // "Gợi ý bổ sung"

    // 4. Dữ liệu thô và thời gian làm việc
    private String rawText;
    private long processingTimeMs;

}