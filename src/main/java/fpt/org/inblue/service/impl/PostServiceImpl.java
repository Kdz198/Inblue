package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.mapper.PostMapper;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.PostCommentRequest;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import fpt.org.inblue.model.dto.request.PostLikeRequest;
import fpt.org.inblue.model.dto.response.PostCommentResponse;
import fpt.org.inblue.model.dto.response.PostLikeResponse;
import fpt.org.inblue.model.dto.response.PostResponse;
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
    public List<PostResponse> getPublishPost() {
        return getAllPost().stream()
                .filter(p -> p.getPost().getStatus() == PostStatus.PUBLISHED)
                .toList();
    }

    @Override
    public List<PostResponse> getAllPost() {
        List<Post> posts = postRepository.findAll();

        List<PostResponse> responses = new ArrayList<>();
        for(Post post : posts) {
            PostResponse response = new PostResponse();
            response.setPost(post);
            response.setPostLikes(postLikeRepository.findAllByPostPostId(post.getPostId()));
            response.setLikeCount(postLikeRepository.countByPostPostId(post.getPostId()));
            response.setPostComments(getCommentsByPostId(post.getPostId()));
            response.setCommentCount(postCommentRepository.countByPostPostId(post.getPostId()));
            responses.add(response);
        }
        return responses;
    }


    @Override
    public PostLike likePost(PostLikeRequest request) {
        if (postLikeRepository.existsByPostPostIdAndUserId(request.getPostId(), request.getUserId())) {
            throw new RuntimeException("User đã like bài viết này rồi");
        }
        Post post = getPostById(request.getPostId());
        User user = userService.getById(request.getUserId());
        PostLike postLike = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        return postLikeRepository.save(postLike);
    }

    @Override
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
        List<PostLikeResponse> responses = new ArrayList<>();
        for(PostLike like : likes){
            PostLikeResponse response = mapLikeToResponse(like);
            responses.add(response);
        }
        return responses;
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
    public int countLikes(int postId) {
        return postLikeRepository.countByPostPostId(postId);
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
            PostComment parentComment = postCommentRepository.findById(request.getParentCommentId()).orElseThrow(() -> new RuntimeException("Parent comment không tồn tại"));
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

        List<PostCommentResponse> comments = new ArrayList<>();
        for(PostComment reply : replies){
            PostCommentResponse response = this.mapCommentToResponse(reply, false);
            comments.add(response);
        }
        return comments;
    }

    @Override
    public int countComments(int postId) {
        return postCommentRepository.countByPostPostId(postId);
    }

    private PostCommentResponse mapCommentToResponse(PostComment comment, boolean includeReplies) {
        PostCommentResponse response = new PostCommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPost().getPostId());
        response.setUserId(comment.getUser().getId());
        response.setUserName(comment.getUser().getName());
        response.setUserAvatar(comment.getUser().getAvatarUrl());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        } else {
            response.setParentCommentId(null);
        }

        if (includeReplies) {
            // Lấy danh sách comment con từ DB
            List<PostComment> repliesFromDB = postCommentRepository
                    .findAllByParentCommentIdOrderByCreatedAtAsc(comment.getId());

            List<PostCommentResponse> replyResponses = new ArrayList<>();

            // Lặp qua danh sách comment con
            for (PostComment reply : repliesFromDB) {
                PostCommentResponse convertedReply = mapCommentToResponse(reply, true);
                replyResponses.add(convertedReply);
            }
            response.setReplies(replyResponses);
        } else {
            response.setReplies(new ArrayList<>());
        }

        return response;
    }
}
