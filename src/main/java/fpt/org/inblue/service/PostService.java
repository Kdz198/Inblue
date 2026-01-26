package fpt.org.inblue.service;

import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import fpt.org.inblue.model.enums.PostStatus;

import java.io.IOException;
import java.util.List;

public interface PostService {
    Post createPost(PostCreateRequest post) throws IOException;
    Post getPostById(int postId);
    void changeStatus(int postId, PostStatus status);
    List<Post> getPublishPost();
    List<Post> getAllPost();
}
