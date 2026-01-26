package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.mapper.PostMapper;
import fpt.org.inblue.model.Major;
import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import fpt.org.inblue.model.enums.PostStatus;
import fpt.org.inblue.repository.PostRepository;
import fpt.org.inblue.service.MajorService;
import fpt.org.inblue.service.PostService;
import fpt.org.inblue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private MajorService majorService;
    @Autowired
    private UserService userService;

    @Override
    public Post createPost(PostCreateRequest post) throws IOException {
        Major major = majorService.getMajorById(post.getMajorId());
        User user = userService.getById(post.getAuthorId());
        Map<String,String> uploadResult = cloudinaryService.uploadImg(post.getCoverImg());
        String url = uploadResult.get("secure_url");
        String public_id = uploadResult.get("public_id");
        Post saved = postMapper.toEntity(post);
        saved.setPublic_id(public_id);
        saved.setCoverImgUrl(url);
        saved.setMajor(major);
        saved.setAuthor(user);
        return postRepository.save(saved);
    }

    @Override
    public Post getPostById(int postId) {
        return null;
    }

    @Override
    public void changeStatus(int postId, PostStatus status) {

    }

    @Override
    public List<Post> getPublishPost() {
        return List.of();
    }

    @Override
    public List<Post> getAllPost() {
        return List.of();
    }
}
