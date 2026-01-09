package app.ai.service.cv.skill.recommendation.dto;

import java.util.List;

/**
 * DTO cho skill recommendation
 * 
 * FIELDS:
 * - skillName: tên skill được recommend
 * - priority: mức độ ưu tiên (1-10)
 * - reason: lý do gợi ý
 * - relatedTo: danh sách skills hiện tại mà skill này liên quan đến
 */
public class SkillRecommendation {
    private String skillName;           // Skill được recommend
    private int priority;               // Priority (1-10)
    private String reason;              // Lý do gợi ý
    private List<String> relatedTo;     // Liên quan đến skills nào

    // Getter & Setter cho skillName
    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    // Getter & Setter cho priority
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    // Getter & Setter cho reason
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // Getter & Setter cho relatedTo
    public List<String> getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(List<String> relatedTo) {
        this.relatedTo = relatedTo;
    }

    // toString() để in ra thông tin recommendation
    @Override
    public String toString() {
        return "SkillRecommendation{" +
                "skillName='" + skillName + '\'' +
                ", priority=" + priority +
                ", reason='" + reason + '\'' +
                ", relatedTo=" + relatedTo +
                '}';
    }
}

