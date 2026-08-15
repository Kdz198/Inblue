package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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

    @Schema(type = "string", format = "binary")
    private MultipartFile coverImg;

    @Schema
    private List<String> tags;

    @Schema(example = "PUBLISHED || DRAFT || ARCHIVED")
    private PostStatus status;
}
