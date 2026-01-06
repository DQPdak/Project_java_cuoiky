package app.ai.service.cv.skill.recommendation.dto;

import java.util.List;

//DTO chứa KẾT QUẢ phân tích gap
public class GapAnalysis {
     private int totalRequired;              // Tổng số skills required
    private int matchingCount;              // Số skills matching
    private int missingCount;               // Số skills missing
    private int extraCount;                 // Số extra skills
    private double coveragePercentage;      // % coverage
    
    private List<String> matchingSkills;    // Skills match
    private List<String> missingSkills;     // Skills thiếu
    private List<String> extraSkills;       // Extra skills
    
    // Getters & Setters
    public int getTotalRequired() { return totalRequired; }
    public void setTotalRequired(int total) { this.totalRequired = total; }
    
    public int getMatchingCount() { return matchingCount; }
    public void setMatchingCount(int count) { this.matchingCount = count; }
    
    public int getMissingCount() { return missingCount; }
    public void setMissingCount(int count) { this.missingCount = count; }
    
    public int getExtraCount() { return extraCount; }
    public void setExtraCount(int count) { this.extraCount = count; }
    
    public double getCoveragePercentage() { return coveragePercentage; }
    public void setCoveragePercentage(double percentage) { 
        this.coveragePercentage = percentage; 
    }
    
    public List<String> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(List<String> skills) { 
        this.matchingSkills = skills; 
    }
    
    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> skills) { 
        this.missingSkills = skills; 
    }
    
    public List<String> getExtraSkills() { return extraSkills; }
    public void setExtraSkills(List<String> skills) { 
        this.extraSkills = skills; 
    }
    
    @Override
    public String toString() {
        return "GapAnalysis{" +
               "coverage=" + String.format("%.1f%%", coveragePercentage) +
               ", matching=" + matchingCount + "/" + totalRequired +
               ", missing=" + missingCount +
               ", extra=" + extraCount +
               '}';
}
}
