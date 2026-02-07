package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.mapper.PostMapper;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.PostCommentRequest;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import fpt.org.inblue.model.dto.request.PostLikeRequest;
import fpt.org.inblue.model.dto.response.PostCommentResponse;
import fpt.org.inblue.model.dto.response.PostLikeResponse;
import fpt.org.inblue.model.enums.PostStatus;
import fpt.org.inblue.repository.PostCommentRepository;
import fpt.org.inblue.repository.PostLikeRepository;
import fpt.org.inblue.repository.PostRepository;
import fpt.org.inblue.service.MajorService;
import fpt.org.inblue.service.PostService;
import fpt.org.inblue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private PostLikeRepository postLikeRepository;
    @Autowired
    private PostCommentRepository postCommentRepository;

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
        return postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public void changeStatus(int postId, PostStatus status) {
        if(postRepository.existsById(postId)) {
            Post post = getPostById(postId);
            post.setStatus(status);
            postRepository.save(post);
        }
        else{
            throw new RuntimeException("Post not found");
        }
    }

    @Override
    public List<Post> getPublishPost() {
        return postRepository.findAllByStatus(PostStatus.PUBLISHED);
    }

    @Override
    public List<Post> getAllPost() {
        return postRepository.findAll();
    }


    @Override
    public PostLike likePost(PostLikeRequest request) {
        if (postLikeRepository.existsByPostPostIdAndUserId(request.getPostId(), request.getUserId())) {
            throw new RuntimeException("User đã like bài viết này rồi");
        }

        Post post = getPostById(request.getPostId());
        User user = userService.getById(request.getUserId());

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);

        return postLikeRepository.save(postLike);
    }

    @Override
    @Transactional
    public void unlikePost(int postId, int userId) {
        if (!postLikeRepository.existsByPostPostIdAndUserId(postId, userId)) {
            throw new RuntimeException("User chưa like bài viết này");
        }
        postLikeRepository.deleteByPostPostIdAndUserId(postId, userId);
    }

    @Override
    public boolean isLiked(int postId, int userId) {
        return postLikeRepository.existsByPostPostIdAndUserId(postId, userId);
    }

    @Override
    public List<PostLikeResponse> getLikesByPostId(int postId) {
        List<PostLike> likes = postLikeRepository.findAllByPostPostId(postId);
        return likes.stream()
                .map(this::mapLikeToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public int countLikes(int postId) {
        return postLikeRepository.countByPostPostId(postId);
    }

    private PostLikeResponse mapLikeToResponse(PostLike postLike) {
        return PostLikeResponse.builder()
                .id(postLike.getId())
                .postId(postLike.getPost().getPostId())
                .userId(postLike.getUser().getId())
                .userName(postLike.getUser().getName())
                .userAvatar(postLike.getUser().getAvatarUrl())
                .createdAt(postLike.getCreatedAt())
                .build();
    }


    @Override
    public PostComment createComment(PostCommentRequest request) {
        Post post = getPostById(request.getPostId());
        User user = userService.getById(request.getUserId());

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.getContent());

        if (request.getParentCommentId() != null) {
            PostComment parentComment = postCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment không tồn tại"));
            comment.setParentComment(parentComment);
        }

        return postCommentRepository.save(comment);
    }

    @Override
    public PostComment updateComment(int commentId, String content) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
        comment.setContent(content);
        return postCommentRepository.save(comment);
    }

    @Override
    public void deleteComment(int commentId) {
        if (!postCommentRepository.existsById(commentId)) {
            throw new RuntimeException("Comment không tồn tại");
        }
        postCommentRepository.deleteById(commentId);
    }

    @Override
    public PostCommentResponse getCommentById(int commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
        return mapCommentToResponse(comment, true);
    }

    @Override
    public List<PostCommentResponse> getCommentsByPostId(int postId) {
        List<PostComment> rootComments = postCommentRepository
                .findAllByPostPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId);

        return rootComments.stream()
                .map(comment -> mapCommentToResponse(comment, true))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostCommentResponse> getReplies(int parentCommentId) {
        List<PostComment> replies = postCommentRepository
                .findAllByParentCommentIdOrderByCreatedAtAsc(parentCommentId);

        return replies.stream()
                .map(comment -> mapCommentToResponse(comment, false))
                .collect(Collectors.toList());
    }

    @Override
    public int countComments(int postId) {
        return postCommentRepository.countByPostPostId(postId);
    }

    private PostCommentResponse mapCommentToResponse(PostComment comment, boolean includeReplies) {
        PostCommentResponse response = PostCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getPostId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .userAvatar(comment.getUser().getAvatarUrl())
                .content(comment.getContent())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();

        if (includeReplies) {
            List<PostComment> replies = postCommentRepository
                    .findAllByParentCommentIdOrderByCreatedAtAsc(comment.getId());
            if (!replies.isEmpty()) {
                response.setReplies(replies.stream()
                        .map(reply -> mapCommentToResponse(reply, true))
                        .collect(Collectors.toList()));
            } else {
                response.setReplies(new ArrayList<>());
            }
        }

        return response;
    }
}
