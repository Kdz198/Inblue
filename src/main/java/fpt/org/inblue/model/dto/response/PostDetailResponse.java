package fpt.org.inblue.model.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailResponse implements Serializable {
    int postId;
    String title;
    String content;
    String summary;
    String status;
    Instant creationDate;
    Instant lastModifiedDate;
    String coverImgUrl;
    List<String> tags;
    AuthorResponse author;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorResponse implements Serializable {
        Integer id;
        String role;
        String name;
        String avatar;
    }
}
