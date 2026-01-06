package app.ai.service.cv.skill.recommendation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.ai.service.cv.skill.recommendation.component.GapSkillFinder;
import app.ai.service.cv.skill.recommendation.component.GapSkillMapper;
import app.ai.service.cv.skill.recommendation.component.GapSkillPriorityEvaluator;
import app.ai.service.cv.skill.recommendation.component.GapSkillSorter;
import app.ai.service.cv.skill.recommendation.component.SkillGapAnalyzer;
import app.ai.service.cv.skill.recommendation.dto.GapAnalysis;
import app.ai.service.cv.skill.recommendation.dto.SkillRecommendation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CLASS: GapSkillOrchestrator
 * ----------------------------------------------------------------------------
 * CHỨC NĂNG: Điều phối luồng xử lý tìm và gợi ý kỹ năng thiếu.
 * SINGLE RESPONSIBILITY: Chỉ lo việc kết nối các bước (workflow), không lo logic chi tiết.
 * ----------------------------------------------------------------------------
 */
@Service
public class GapSkillOrchestrator {

    // Tiêm các "công nhân" đã tách vào đây
    @Autowired private GapSkillFinder finder;
    @Autowired private GapSkillMapper mapper;
    @Autowired private GapSkillPriorityEvaluator evaluator;
    @Autowired private GapSkillSorter sorter;
    @Autowired private SkillGapAnalyzer analyzer;

    /**
     * Luồng xử lý chính để lấy danh sách gợi ý lấp Gap
     */
    public List<SkillRecommendation> getRecommendations(
            List<String> currentSkills, 
            List<String> requiredSkills, 
            List<String> criticalSkills) {

        // Bước 1: Tìm danh sách các chuỗi tên skill bị thiếu
        List<String> missingSkillNames = finder.findMissing(currentSkills, requiredSkills);

        // Bước 2: Chuyển đổi String thành Object DTO và Đánh giá độ ưu tiên
        List<SkillRecommendation> recommendations = missingSkillNames.stream()
                .map(name -> {
                    SkillRecommendation rec = mapper.map(name);
                    evaluator.evaluate(rec, criticalSkills); // Tính điểm priority
                    return rec;
                })
                .collect(Collectors.toList());

        // Bước 3: Sắp xếp kết quả cuối cùng
        sorter.sort(recommendations);

        return recommendations;
    }

    /**
     * Luồng xử lý để lấy báo cáo phân tích tổng quát
     */
    public GapAnalysis getGapAnalysis(List<String> currentSkills, List<String> requiredSkills) {
        return analyzer.analyzeGap(currentSkills, requiredSkills);
    }
}
