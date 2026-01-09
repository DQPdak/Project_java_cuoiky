package app.ai.service.cv.scoring;

import org.springframework.stereotype.Service;

import app.ai.service.cv.scoring.compoment.*;
import app.ai.service.cv.scoring.dto.CandidateScore;

import java.util.List;
import java.util.Map;

@Service
public class CvScoringService {

    private final rankCandidates rankComponent; // Điều phối xếp hạng và điểm tổng
    private final CategoryScore categoryComponent; // Điều phối phân tích theo nhóm kỹ năng
    private final ScoreExplanation explanationComponent; // Điều phối hiển thị thông báo

    public CvScoringService(rankCandidates rankComponent, 
                            CategoryScore categoryComponent) {
        this.rankComponent = rankComponent;
        this.categoryComponent = categoryComponent;
        this.explanationComponent = new ScoreExplanation(); // Khởi tạo trực tiếp
    }

    /**
     * 1. Xếp hạng và Hiển thị thông báo chi tiết cho từng người
     */
    public List<CandidateScore> getRankedCandidatesWithExplainer(
            Map<String, List<String>> candidates, 
            List<String> requiredSkills) {
        
        // Bước 1: Gọi rank để có danh sách đã sắp xếp và có sẵn SkillScore bên trong
        List<CandidateScore> rankedList = rankComponent.rank(candidates, requiredSkills);
        
        // Bước 2: Duyệt danh sách để in/tạo thông báo dựa trên dữ liệu đã có
        for (CandidateScore candidate : rankedList) {
            String message = explanationComponent.generateScoreExplanation(candidate.getScore());
            System.out.println("Ứng viên: " + candidate.getCandidateId());
            System.out.println(message); 
            // Bạn có thể lưu message này vào một field mới trong CandidateScore nếu cần
        }
        
        return rankedList;
    }

    /**
     * 2. Phân tích chuyên sâu theo Category (Ví dụ dùng cho biểu đồ)
     */
    public Map<String, Double> getCategoryAnalysis(List<String> candidateSkills, 
                                                   Map<String, List<String>> requiredSkillsByCategory) {
        // Trả về Map<Category, Score> để biết ứng viên mạnh/yếu ở đâu
        return categoryComponent.calculate(candidateSkills, requiredSkillsByCategory);
    }
}