package app.ai.dto;

/**
 * CHỨC NĂNG: Chứa điểm số kỹ năng của ứng viên
 */
public class SkillScore {
    private double baseScore;        // Điểm cơ bản (exact matches)
    private double bonusScore;       // Điểm bonus (extra skills)
    private double totalScore;       // Tổng điểm
    private int exactMatches;        // Số skills match chính xác
    private int totalRequired;       // Tổng số skills required
    private int extraSkillCount;     // Số extra skills
    private String level;           // Mức độ đánh giá (EXCELLENT, GOOD, FAIR, POOR)
    
    // Getters & Setters
    public double getBaseScore() { return baseScore; }
    public void setBaseScore(double score) { this.baseScore = score; }
    
    public double getBonusScore() { return bonusScore; }
    public void setBonusScore(double score) { this.bonusScore = score; }
    
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double score) { this.totalScore = score; }
    
    public int getExactMatches() { return exactMatches; }
    public void setExactMatches(int matches) { this.exactMatches = matches; }
    
    public int getTotalRequired() { return totalRequired; }
    public void setTotalRequired(int total) { this.totalRequired = total; }
    
    public int getExtraSkillCount() { return extraSkillCount; }
    public void setExtraSkillCount(int count) { this.extraSkillCount = count; }
    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }
    
}
