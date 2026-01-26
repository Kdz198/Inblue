package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Post create request")
public class PostCreateRequest {
    @Schema
    private String title;

    @Schema
    private String content;

    @Schema
    private String summary;

    @Schema
    private int authorId;

    @Schema
    private int majorId;

    @Schema(type = "string", format = "binary")
    private MultipartFile coverImg;

    @Schema
    private List<String> tags;

    @Schema(example = "PUBLISHED || DRAFT || ARCHIVED")
    private PostStatus status;
}
