package fpt.org.inblue.model.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCommentResponse {
    int id;
    String userName;
    String userAvatar;
    String content;
    int parentCommentId;
    Timestamp createdAt;
    Timestamp updatedAt;
}

