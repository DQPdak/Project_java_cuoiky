package app.ai.service.cv.dto;

import app.ai.service.cv.extractorcontact.dto.ContactInfo;
import app.ai.service.cv.extractorexperience.dto.ExperienceDTO;
import app.ai.service.cv.skill.component.recomment.dto.ComprehensiveRecommendation;
import app.ai.service.cv.skill.component.scoring.dto.SkillScore; // Import class SkillScore
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVAnalysisResult {
    // 1. Thông tin Liên lạc
    private ContactInfo contactInfo;

    // 2. Thông tin kinh nghiệm
    private ExperienceDTO experience;

    // 3. Danh sách kỹ năng (THÊM MỚI ĐỂ SỬA LỖI getSkills())
    private List<SkillScore> skills;

    // 4. Thông tin tương thích với công việc (Dùng cho chức năng ứng tuyển sau này)
    private Object matchScore;
    private ComprehensiveRecommendation recommendations;

    // 5. Dữ liệu thô
    private String rawText;
    private long processingTimeMs;
}