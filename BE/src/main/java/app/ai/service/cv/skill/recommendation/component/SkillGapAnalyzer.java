package app.ai.service.cv.skill.recommendation.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.recommendation.dto.GapAnalysis;

import java.util.Set;
import java.util.List;
/**
 * CHỨC NĂNG:
 * - Chuyên phân tích sự sai khác giữa các tập hợp kỹ năng.
 * - Chỉ lo so sánh Skills hiện có vs Skills yêu cầu.
 */
@Component
public class SkillGapAnalyzer {
    public GapAnalysis analyzeGap(List<String> currentSkills,
                                  List<String> requiredSkills) {
        
        // Normalize
        Set<String> currentSet = currentSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        
        Set<String> requiredSet = requiredSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        
        // Tìm matching
        Set<String> matching = new HashSet<>(requiredSet);
        matching.retainAll(currentSet);
        
        // Tìm missing
        Set<String> missing = new HashSet<>(requiredSet);
        missing.removeAll(currentSet);
        
        // Tìm extra
        Set<String> extra = new HashSet<>(currentSet);
        extra.removeAll(requiredSet);
        
        // Build analysis
        GapAnalysis analysis = new GapAnalysis();
        analysis.setTotalRequired(requiredSet.size());
        analysis.setMatchingCount(matching.size());
        analysis.setMissingCount(missing.size());
        analysis.setExtraCount(extra.size());
        analysis.setMatchingSkills(new ArrayList<>(matching));
        analysis.setMissingSkills(new ArrayList<>(missing));
        analysis.setExtraSkills(new ArrayList<>(extra));
        
        // Calculate coverage
        double coverage = requiredSet.isEmpty() ? 0.0 :
            (double) matching.size() / requiredSet.size() * 100;
        analysis.setCoveragePercentage(coverage);
        
        return analysis;
    }
}
