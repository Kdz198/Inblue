package fpt.org.inblue.model.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PostResponse {
    PostDetailResponse post;
    int likeCount;
    int commentCount;
    List<PostLikeResponse> postLikes;
    List<PostCommentResponse> postComments;
}
