package app.ai.Service.cv.scoring.Compoment;

import app.ai.dto.CandidateScore;
import app.ai.dto.SkillScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//  CHỨC NĂNG: Xếp hạng các ứng viên dựa trên điểm kỹ năng tổng hợp

public class rankCandidates {
    public List<CandidateScore> rank(
            Map<String, List<String>> candidates,
            List<String> requiredSkills) {
        List<CandidateScore> scores = new ArrayList<>();
        // Tính điểm cho từng candidate
        for (Map.Entry<String, List<String>> entry : candidates.entrySet()) {
            TotalScore total = new TotalScore();
            String candidateId = entry.getKey();
            List<String> candidateSkills = entry.getValue();
            
            SkillScore score = total.calculate(candidateSkills, requiredSkills);
            
            CandidateScore candidateScore = new CandidateScore();
            candidateScore.setCandidateId(candidateId);
            candidateScore.setScore(score);
            
            scores.add(candidateScore);
        }
        
        // Sắp xếp theo điểm giảm dần
        scores.sort((a, b) -> Double.compare(
            b.getScore().getTotalScore(), 
            a.getScore().getTotalScore()
        ));
        
        return scores;
    }
    
}
