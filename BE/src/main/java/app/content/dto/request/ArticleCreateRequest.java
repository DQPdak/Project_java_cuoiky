package app.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ArticleCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    @Size(max = 50)
    private String category;

    // optional: DRAFT/PUBLISHED, nếu null thì DRAFT
    private String status;
}
