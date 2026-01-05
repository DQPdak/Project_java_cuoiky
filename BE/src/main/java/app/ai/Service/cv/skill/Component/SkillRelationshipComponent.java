package app.ai.Service.cv.skill.Component;

/**
 * CHỨC NĂNG: sử lý các logic liên quan đến mối quan hệ giữa các skill
 * Tìm các kỹ năng liên quan đến kỹ năng cụ thể được khai báo
 * Thiết lập mối quan hệ giữa kĩ năng được gợi ý với kỹ năng cụ thể
 */

import app.ai.Service.cv.skill.Model.Skill;
import app.ai.Service.cv.skill.Model.SkillRelationship;
import app.ai.Service.cv.skill.Repository.ISkillRelationshipRepository;
import app.ai.Service.cv.skill.Repository.ISkillRepository;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Comparator;


@Component
public class SkillRelationshipComponent {

    private final ISkillRepository skillRepo;
    private final ISkillRelationshipRepository relationshipRepo;

    public SkillRelationshipComponent(ISkillRepository skillRepo,
                                    ISkillRelationshipRepository relationshipRepo) {
        this.skillRepo = skillRepo;
        this.relationshipRepo = relationshipRepo;
    }
    /**
     * Tìm kỹ năng theo tện
     * Lấy các kĩ năng liên quan đến kỹ năng đó đồng thời xuất danh sách
     */
    public List<Skill> getRelatedSkills(String skillName) {
        Skill skill = skillRepo.findByNameIgnoreCase(skillName)
                               .orElseThrow(() -> new RuntimeException("Skill not found"));
        return relationshipRepo.findBySkill(skill) //gọi skill để lấy các mối quan hệ liên quan
                               .stream()
                               .map(SkillRelationship::getRelatedSkill) // trích xuất các kỹ năng liên quan
                               .toList();
    }

    // Tạo mối quan hệ mới giữa 2 skill
    public void addRelationship(String skillName, String relatedSkillName) {
        Skill skill = skillRepo.findByNameIgnoreCase(skillName).orElseThrow();
        Skill related = skillRepo.findByNameIgnoreCase(relatedSkillName).orElseThrow();

        SkillRelationship rel = new SkillRelationship();
        rel.setSkill(skill);
        rel.setRelatedSkill(related);
        relationshipRepo.save(rel);
    }

    /**
     * Xóa toàn bộ quan hệ của một skill
     */
    public boolean removeRelationship(String skillName) {
        Skill skill = skillRepo.findByNameIgnoreCase(skillName)
                               .orElseThrow(() -> new RuntimeException("Skill not found"));
        List<SkillRelationship> relations = relationshipRepo.findBySkill(skill);
        if (relations.isEmpty()) return false;
        relationshipRepo.deleteAll(relations);
        return true;
    }

     /**
     * Xóa một quan hệ cụ thể giữa skill và relatedSkill
     */
    public boolean removeRelatedSkill(String skillName, String relatedSkillName) {
        Skill skill = skillRepo.findByNameIgnoreCase(skillName).orElseThrow();
        Skill related = skillRepo.findByNameIgnoreCase(relatedSkillName).orElseThrow();
        return relationshipRepo.findBySkill(skill).stream()
                .filter(r -> r.getRelatedSkill().equals(related))
                .findFirst()
                .map(r -> {
                    relationshipRepo.delete(r);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Đếm tổng số skill có quan hệ
     */
    public int getTotalRelationshipCount() {
        return (int) relationshipRepo.count();
    }

    /**
     * Tìm skill có nhiều quan hệ nhất
     */
    public String getMostConnectedSkill() {
        return skillRepo.findAll().stream()
                .max(Comparator.comparingInt(s -> relationshipRepo.findBySkill(s).size()))
                .map(Skill::getName)
                .orElse(null);
    }

    /**
     * Tìm skill có ít quan hệ nhất
     */
    public String getLeastConnectedSkill() {
        return skillRepo.findAll().stream()
                .min(Comparator.comparingInt(s -> relationshipRepo.findBySkill(s).size()))
                .map(Skill::getName)
                .orElse(null);
    }

}

