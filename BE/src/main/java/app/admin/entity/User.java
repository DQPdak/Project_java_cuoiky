package app.admin.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id @GeneratedValue
    private Long id;

    private String full_name;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private Role role; // CANDIDATE, RECRUITER, ADMIN

    @Enumerated(EnumType.STRING)
    private UserStatus status; // ACTIVE, LOCKED

    private LocalDate created_at;
}
