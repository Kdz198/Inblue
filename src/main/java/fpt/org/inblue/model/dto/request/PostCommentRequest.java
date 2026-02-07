package fpt.org.inblue.model.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCommentRequest {
    int postId;
    int userId;
    String content;
    Integer parentCommentId; // null nếu là comment gốc, có giá trị nếu là reply
}

