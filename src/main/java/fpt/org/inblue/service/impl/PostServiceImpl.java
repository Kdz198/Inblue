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
        saved.setLikes(new ArrayList<>());
        saved.setComments(new ArrayList<>());
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
        List<Post> posts = postRepository.findAllByStatus(PostStatus.PUBLISHED);
        return mapPostToResponse(posts);
    }

    List<PostResponse> mapPostToResponse(List<Post> posts) {
        List<PostResponse> responses = new ArrayList<>();
        for(Post post : posts) {
            PostResponse response = new PostResponse();
            response.setPost(post);
            response.setLikeCount(post.getLikes() != null ? post.getLikes().size() : 0);
            response.setCommentCount(post.getComments() != null ? post.getComments().size() : 0);
            responses.add(response);
        }
        return responses;
    }

    @Override
    public List<PostResponse> getAllPost() {
        List<Post> posts = postRepository.findAll();
        return mapPostToResponse(posts);
    }


    @Override
    @Transactional
    public PostLike likePost(PostLikeRequest request) {
        Post post = getPostById(request.getPostId());
        User user = userService.getById(request.getUserId());

        // Kiểm tra user đã like chưa
        boolean alreadyLiked = post.getLikes().stream()
                .anyMatch(like -> like.getUser().getId() == request.getUserId());
        if (alreadyLiked) {
            throw new RuntimeException("User đã like bài viết này rồi");
        }

        // Tạo like mới và thêm vào post
        PostLike postLike = PostLike.builder()
                .user(user)
                .build();

        post.getLikes().add(postLike);
        postRepository.save(post);

        return postLike;
    }

    @Override
    @Transactional
    public void unlikePost(int postId, int userId) {
        Post post = getPostById(postId);

        boolean removed = post.getLikes().removeIf(like -> like.getUser().getId() == userId);
        if (!removed) {
            throw new RuntimeException("User chưa like bài viết này");
        }

        postRepository.save(post);
    }

    @Override
    public boolean isLiked(int postId, int userId) {
        Post post = getPostById(postId);
        return post.getLikes().stream()
                .anyMatch(like -> like.getUser().getId() == userId);
    }

    @Override
    public List<PostLikeResponse> getLikesByPostId(int postId) {
        Post post = getPostById(postId);
        return post.getLikes().stream()
                .map(like -> mapLikeToResponse(like, postId))
                .collect(Collectors.toList());
    }

    private PostLikeResponse mapLikeToResponse(PostLike postLike, int postId) {
        return PostLikeResponse.builder()
                .id(postLike.getId())
                .postId(postId)
                .userId(postLike.getUser().getId())
                .userName(postLike.getUser().getName())
                .userAvatar(postLike.getUser().getAvatarUrl())
                .createdAt(postLike.getCreatedAt())
                .build();
    }

    @Override
    public int countLikes(int postId) {
        Post post = getPostById(postId);
        return post.getLikes() != null ? post.getLikes().size() : 0;
    }

    @Override
    @Transactional
    public PostComment createComment(PostCommentRequest request) {
        Post post = getPostById(request.getPostId());
        User user = userService.getById(request.getUserId());

        PostComment comment = new PostComment();
        comment.setUser(user);
        comment.setContent(request.getContent());

        // Nếu là reply
        if (request.getParentCommentId() != null) {
            PostComment parentComment = post.getComments().stream()
                    .filter(c -> c.getId() == request.getParentCommentId())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Parent comment không tồn tại"));
            comment.setParentComment(parentComment);
        }

        post.getComments().add(comment);
        postRepository.save(post);

        return comment;
    }

    @Override
    @Transactional
    public PostComment updateComment(int commentId, String content) {
        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            for (PostComment comment : post.getComments()) {
                if (comment.getId() == commentId) {
                    comment.setContent(content);
                    postRepository.save(post);
                    return comment;
                }
            }
        }
        throw new RuntimeException("Comment không tồn tại");
    }

    @Override
    @Transactional
    public void deleteComment(int commentId) {
        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            post.getComments().removeIf(c -> c.getParentComment() != null && c.getParentComment().getId() == commentId);
            boolean removed = post.getComments().removeIf(c -> c.getId() == commentId);
            if (removed) {
                postRepository.save(post);
                return;
            }
        }
        throw new RuntimeException("Comment không tồn tại");
    }

    @Override
    public PostCommentResponse getCommentById(int commentId) {
        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            for (PostComment comment : post.getComments()) {
                if (comment.getId() == commentId) {
                    return mapCommentToResponse(comment, post.getPostId(), post.getComments(), true);
                }
            }
        }
        throw new RuntimeException("Comment không tồn tại");
    }

    @Override
    public List<PostCommentResponse> getCommentsByPostId(int postId) {
        Post post = getPostById(postId);
        List<PostCommentResponse> responseList = new ArrayList<>();
        List<PostComment> allComments = post.getComments();
        for (PostComment c : allComments) {
            if (c.getParentComment() == null) {
                PostCommentResponse res = mapCommentToResponse(c, postId, allComments, true);
                responseList.add(res);
            }
        }
        return responseList;
    }

    @Override
    public List<PostCommentResponse> getReplies(int parentCommentId) {
        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            boolean hasParent = post.getComments().stream()
                    .anyMatch(c -> c.getId() == parentCommentId);
            if (hasParent) {
                return post.getComments().stream()
                        .filter(c -> c.getParentComment() != null && c.getParentComment().getId() == parentCommentId)
                        .map(c -> mapCommentToResponse(c, post.getPostId(), post.getComments(), false))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public int countComments(int postId) {
        Post post = getPostById(postId);
        return post.getComments() != null ? post.getComments().size() : 0;
    }

    private PostCommentResponse mapCommentToResponse(PostComment comment, int postId, List<PostComment> allComments, boolean includeReplies) {
        PostCommentResponse response = new PostCommentResponse();
        response.setId(comment.getId());
        response.setPostId(postId);
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
            // Lấy replies
            List<PostCommentResponse> replies = allComments.stream()
                    .filter(c -> c.getParentComment() != null && c.getParentComment().getId() == comment.getId())
                    .map(c -> mapCommentToResponse(c, postId, allComments, true))
                    .collect(Collectors.toList());
            response.setReplies(replies);
        } else {
            response.setReplies(new ArrayList<>());
        }

        return response;
    }
}
