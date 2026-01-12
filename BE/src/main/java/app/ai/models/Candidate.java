package app.ai.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidates")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contact info
    private String email;
    private String phoneNumber;

    // List String cho Skills (Lưu mảng đơn giản)
    @ElementCollection
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "candidate_id"))
    @Column(name = "skill_name")
    private List<String> skills;

    // Quan hệ 1-N với Experience
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Experience> experiences;
}