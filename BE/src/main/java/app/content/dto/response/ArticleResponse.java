package app.content.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
