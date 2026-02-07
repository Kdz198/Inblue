package fpt.org.inblue.model.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCommentResponse {
    int id;
    int postId;
    int userId;
    String userName;
    String userAvatar;
    String content;
    Integer parentCommentId;
    Timestamp createdAt;
    Timestamp updatedAt;
    List<PostCommentResponse> replies;
}

