package app.ai.service.cv.skill.extractorskill.component;

/**
 * CHỨC NĂNG:
 * - Tạo 1 database kỹ năng mẫu để phục vụ việc trích xuất kỹ năng từ CV
 * - database này có thể mở rộng, bổ sung thêm kỹ năng mới trong tương lai
 * - đây không phải data theo kiểu sql mà chỉ là 1 class khái niệm lưu trữ kỹ năng tạm thời
 */

import org. springframework.stereotype.Component;

import app.ai.service.cv.skill.extractorskill.model.Skill;
import app.ai.service.cv.skill.extractorskill.model.SkillCategory;
import app.ai.service.cv.skill.extractorskill.repository.ISkillCategoryRepository;
import app.ai.service.cv.skill.extractorskill.repository.ISkillRepository;

import java.util.List;

@Component
public class SkillDatabase {
    private ISkillRepository skillRepository;
    private ISkillCategoryRepository categoryRepository;

    // Constructor để inject repository
    public SkillDatabase(ISkillRepository skillRepository, ISkillCategoryRepository categoryRepository) {
        this.skillRepository = skillRepository;
        this.categoryRepository = categoryRepository;
    }

    // Lấy danh sách kỹ năng theo nhóm
    public List<String> getSkillsByCategory(String categoryName) {
        return skillRepository.findByCategoryNameIgnoreCase(categoryName)
                .stream().map(Skill::getName).toList();
    }

    // Lấy toàn bộ tên nhóm kỹ năng
    public List<String> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(SkillCategory::getName).toList();
    }

    // Kiểm tra kỹ năng có tồn tại không
    public boolean containsSkill(String skillName) {
        return skillRepository.findByNameIgnoreCase(skillName).isPresent();
    }

    // Tìm nhóm kỹ năng của một kỹ năng cụ thể
    public String getCategoryOfSkill(String skillName) {
        return skillRepository.findByNameIgnoreCase(skillName)
                .map(skill -> skill.getCategory().getName())
                .orElse(null);
    }

    // Thêm kỹ năng mới vào nhóm (tự tạo nhóm nếu chưa có)
    public void addSkill(String categoryName, String skillName) {
        SkillCategory category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> {
                    SkillCategory newCat = new SkillCategory();
                    newCat.setName(categoryName);
                    return categoryRepository.save(newCat);
                });

        Skill skill = new Skill();
        skill.setName(skillName.toLowerCase());
        skill.setCategory(category);
        skillRepository.save(skill);
    }

    // Xóa kỹ năng khỏi database
    public boolean removeSkill(String skillName) {
        return skillRepository.findByNameIgnoreCase(skillName)
                .map(skill -> {
                    skillRepository.delete(skill);
                    return true;
                }).orElse(false);
    }

}

