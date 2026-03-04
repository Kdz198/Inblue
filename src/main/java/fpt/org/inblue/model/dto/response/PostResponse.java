package fpt.org.inblue.model.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PostResponse implements Serializable {
    PostDetailResponse post;
    int likeCount;
    int commentCount;
    List<PostLikeResponse> postLikes;
    List<PostCommentResponse> postComments;
}
