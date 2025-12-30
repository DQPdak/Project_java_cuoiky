package app.ai.Service.cv.skill.dto;
/**
 * CHỨC NĂNG: Trả về kết quả khi so sánh 2 danh sách Skill
 */

import java.util.*;

public class SkillMatchResult {
    private Set<String> matchedSkills; // Danh sách kỹ năng khớp
    private Set<String> missingSkills; // Danh sách kỹ năng thiếu
    private Set<String> extraSkills;   // Danh sách kỹ năng thừa
    private int matchCount;          // Số kỹ năng khớp
    private int totalRequired;      // Tổng số kỹ năng yêu cầu
    private double matchPercentage; // Tỷ lệ phần trăm khớp

    // Tính toán tỷ lệ phần trăm khớp (Logic đơn giản bổ trợ cho dữ liệu)
    public double getMatchPercentage() {
        return matchPercentage;
    }
    
    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public Set<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(Set<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
        this.matchCount = (matchedSkills != null) ? matchedSkills.size() : 0;
    }
    public Set<String> getMissingSkills() {
        return missingSkills;
    }
    public void setMissingSkills(Set<String> missingSkills) {
        this.missingSkills = missingSkills;
    }
    public Set<String> getExtraSkills() {
        return extraSkills;
    }
    public void setExtraSkills(Set<String> extraSkills) {
        this.extraSkills = extraSkills;
    }
    public int getMatchCount() {
        return matchCount;
    }
    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }
    public int getTotalRequired() {
        return totalRequired;
    }
    public void setTotalRequired(int totalRequired) {
        this.totalRequired = totalRequired;
    }
    
    @Override
    public String toString() {
        return "Kết Quả So Khớp Kỹ Năng{" +
                "tỷ Lệ Khớp=" + String.format("%.2f", getMatchPercentage()) + "%" +
                ", số Lượng Khớp=" + matchCount +
                ", tổng Số Yêu Cầu=" + totalRequired +
                ", kỹ Năng Thiếu=" + missingSkills +
                '}';
    }
    }

