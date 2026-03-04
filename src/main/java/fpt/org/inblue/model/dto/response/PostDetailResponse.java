package fpt.org.inblue.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Date;
import java.time.Instant;
import java.util.List;

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
    String majorName;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorResponse implements Serializable{
        String name;
        String avatar;
    }
}

