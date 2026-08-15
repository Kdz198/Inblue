package fpt.org.inblue.model.dto.response;

import java.io.Serializable;
import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCommentResponse implements Serializable {
    int id;
    Integer userId;
    String role;
    String userName;
    String userAvatar;
    String content;
    int parentCommentId;
    Timestamp createdAt;
    Timestamp updatedAt;
}
