package fpt.org.inblue.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailResponse {
    int postId;
    String title;
    String content;
    String summary;
    String status;
    Date creationDate;
    Date lastModifiedDate;
    String coverImgUrl;
    List<String> tags;
    AuthorResponse author;
    String majorName;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorResponse {
        String name;
        String avatar;
    }
}

