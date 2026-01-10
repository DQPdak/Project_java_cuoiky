package app.ai.service.cv.extractorexperience;

import org.springframework.stereotype.Service;

import app.ai.service.cv.extractorexperience.component.CompanyParser;
import app.ai.service.cv.extractorexperience.component.LevelDeterminer;
import app.ai.service.cv.extractorexperience.component.YearParser;
import app.ai.service.cv.extractorexperience.dto.ExperienceDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExperienceService {
    private final YearParser yearParser;
    private final CompanyParser companyParser;
    private final LevelDeterminer levelDeterminer;

    /**
     * Phương thức chính để phân tích CV
     */
    public ExperienceDTO analyzeExperience(String cvText) {
        // Gọi Parser lấy số năm
        int totalYears = yearParser.parse(cvText);
        
        // Gọi Parser lấy danh sách công ty
        var companies = companyParser.parse(cvText);
        
        //  Gọi Determiner để chốt Level
        String level = levelDeterminer.determine(totalYears, cvText);
        
        // Đóng gói kết quả trả về
        return new ExperienceDTO(totalYears, companies, level);
    }
}
