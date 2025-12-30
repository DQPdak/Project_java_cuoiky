package app.ai.Service.cv.scoring.dto;

public class CandidateScore {
    private String candidateId;  // ID của candidate
    private SkillScore score;    // Điểm chi tiết
    
    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String id) { this.candidateId = id; }
    
    public SkillScore getScore() { return score; }
    public void setScore(SkillScore score) { this.score = score; }
}
