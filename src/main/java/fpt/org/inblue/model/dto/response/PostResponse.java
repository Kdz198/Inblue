package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.PostLike;
import lombok.Data;

import java.util.List;

@Data
public class PostResponse {
    Post post;
    int likeCount;
    int commentCount;
    List<PostLike> postLikes;
    List<PostCommentResponse> postComments;
}
