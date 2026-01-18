package app.ai.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonIgnore // Chặn loop vô hạn khi trả về JSON
    private InterviewSession session;

    private String sender; // "USER" (Ứng viên) hoặc "AI" (Nhà tuyển dụng)
    
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime sentAt;
}