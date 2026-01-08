package app.ai.service.cv.scoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateScore {
    private String candidateId;  // ID của candidate
    private SkillScore score;    // Điểm chi tiết
}
