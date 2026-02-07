package fpt.org.inblue.service;

import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.PostComment;
import fpt.org.inblue.model.PostLike;
import fpt.org.inblue.model.dto.request.PostCommentRequest;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import fpt.org.inblue.model.dto.request.PostLikeRequest;
import fpt.org.inblue.model.dto.response.PostCommentResponse;
import fpt.org.inblue.model.dto.response.PostLikeResponse;
import fpt.org.inblue.model.enums.PostStatus;

import java.io.IOException;
import java.util.List;

public interface PostService {
    Post createPost(PostCreateRequest post) throws IOException;
    Post getPostById(int postId);
    void changeStatus(int postId, PostStatus status);
    List<Post> getPublishPost();
    List<Post> getAllPost();

    // Like methods
    PostLike likePost(PostLikeRequest request);
    void unlikePost(int postId, int userId);
    boolean isLiked(int postId, int userId);
    List<PostLikeResponse> getLikesByPostId(int postId);
    int countLikes(int postId);

    // Comment methods
    PostComment createComment(PostCommentRequest request);
    PostComment updateComment(int commentId, String content);
    void deleteComment(int commentId);
    PostCommentResponse getCommentById(int commentId);
    List<PostCommentResponse> getCommentsByPostId(int postId);
    List<PostCommentResponse> getReplies(int parentCommentId);
    int countComments(int postId);
}
