package app.ai.service.cv.extractorexperience.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {
    private int totalYears; // Số năm kinh nghiệm
    private List<String> companies; // danh sách công ty đã làm
    private String level; // người mới/trung cấp/kỳ cựu
}
