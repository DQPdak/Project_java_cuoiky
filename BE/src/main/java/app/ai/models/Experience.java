package app.ai.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experiences")
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company; // Tên công ty
    private String role;    // Chức vụ (Vd: Java Developer)
    
    private String startDate; // Ngày bắt đầu
    private String endDate;   // Ngày kết thúc (hoặc "Present")
    
    @Column(columnDefinition = "TEXT") // Cho phép lưu mô tả dài
    private String description;

    // Quan hệ N-1: Nhiều kinh nghiệm thuộc về 1 ứng viên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id") // Tên cột khóa ngoại trong DB
    private Candidate candidate;
}
