package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.mapper.PostMapper;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.PostCommentRequest;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import fpt.org.inblue.model.dto.request.PostLikeRequest;
import fpt.org.inblue.model.dto.response.PostCommentResponse;
import fpt.org.inblue.model.dto.response.PostDetailResponse;
import fpt.org.inblue.model.dto.response.PostLikeResponse;
import fpt.org.inblue.model.dto.response.PostResponse;
import fpt.org.inblue.model.enums.PostStatus;
import fpt.org.inblue.repository.PostRepository;
import fpt.org.inblue.service.MajorService;
import fpt.org.inblue.service.PostService;
import fpt.org.inblue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
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

    public Post getPostById(int postId) {
        return postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public void changeStatus(int postId, PostStatus status) {
        if (postRepository.existsById(postId)) {
            Post post = getPostById(postId);
            post.setStatus(status);
            postRepository.save(post);
        } else {
            throw new RuntimeException("Post not found");
        }
    }

    @Override
    public List<PostResponse> getPublishPost() {
        List<Post> posts = postRepository.findAllByStatus(PostStatus.PUBLISHED);
        return mapPostToResponse(posts);
    }

    @Override
    public PostResponse getPostByPostId(int postId) {
        PostResponse response = new PostResponse();
        Post post = getPostById(postId);
        PostDetailResponse detailResponse = PostDetailResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .summary(post.getSummary())
                .status(post.getStatus() != null ? post.getStatus().name() : null)
                .creationDate(post.getCreationDate())
                .lastModifiedDate(post.getLastModifiedDate())
                .coverImgUrl(post.getCoverImgUrl())
                .tags(post.getTags())
                .author(post.getAuthor() != null ?
                        PostDetailResponse.AuthorResponse.builder()
                                .name(post.getAuthor().getName())
                                .avatar(post.getAuthor().getAvatarUrl())
                                .build() : null)
                .majorName(post.getMajor() != null ? post.getMajor().getMajorName() : null)
                .build();
        response.setPost(detailResponse);
        response.setLikeCount(post.getLikes() != null ? post.getLikes().size() : 0);
        response.setCommentCount(post.getComments() != null ? post.getComments().size() : 0);
        List<PostLikeResponse> likeResponses = new ArrayList<>();
        if (post.getLikes() != null) {
            for (PostLike like : post.getLikes()) {
                PostLikeResponse likeResponse = PostLikeResponse.builder()
                        .userName(like.getUser().getName())
                        .userAvatar(like.getUser().getAvatarUrl())
                        .build();
                likeResponses.add(likeResponse);
            }
        }
        response.setPostLikes(likeResponses);

        // map comment
        List<PostCommentResponse> commentResponses = new ArrayList<>();
        if (post.getComments() != null) {
            for (PostComment c : post.getComments()) {
                PostCommentResponse res = mapCommentToResponse(c);
                commentResponses.add(res);
            }
        }
        response.setPostComments(commentResponses);
        return response;
    }


    List<PostResponse> mapPostToResponse(List<Post> posts) {
        List<PostResponse> responses = new ArrayList<>();
        for (Post post : posts) {
            PostResponse response = new PostResponse();

            // map post
            PostDetailResponse postDetail = PostDetailResponse.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .summary(post.getSummary())
                    .status(post.getStatus() != null ? post.getStatus().name() : null)
                    .creationDate(post.getCreationDate())
                    .lastModifiedDate(post.getLastModifiedDate())
                    .coverImgUrl(post.getCoverImgUrl())
                    .tags(post.getTags())
                    .author(post.getAuthor() != null ?
                            PostDetailResponse.AuthorResponse.builder()
                                    .name(post.getAuthor().getName())
                                    .avatar(post.getAuthor().getAvatarUrl())
                                    .build() : null)
                    .majorName(post.getMajor() != null ? post.getMajor().getMajorName() : null)
                    .build();

            response.setPost(postDetail);
            response.setLikeCount(post.getLikes() != null ? post.getLikes().size() : 0);
            response.setCommentCount(post.getComments() != null ? post.getComments().size() : 0);

            //map like
            List<PostLikeResponse> likeResponses = new ArrayList<>();
            if (post.getLikes() != null) {
                for (PostLike like : post.getLikes()) {
                    PostLikeResponse likeResponse = PostLikeResponse.builder()
                            .userName(like.getUser().getName())
                            .userAvatar(like.getUser().getAvatarUrl())
                            .build();
                    likeResponses.add(likeResponse);
                }
            }
            response.setPostLikes(likeResponses);

            // map comment
            List<PostCommentResponse> commentResponses = new ArrayList<>();
            if (post.getComments() != null) {
                for (PostComment c : post.getComments()) {
                    PostCommentResponse res = mapCommentToResponse(c);
                    commentResponses.add(res);
                }
            }
            response.setPostComments(commentResponses);

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
    public Page<PostResponse> getNewFeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> postPage = postRepository.findAllByOrderByCreationDateDesc(pageable);

        List<Post> posts = postPage.getContent();
        List<PostResponse> mappedResponses = mapPostToResponse(posts);

        return new PageImpl<>(mappedResponses, postPage.getPageable(), postPage.getTotalElements());
    }

    @Override
    @Transactional
    public PostLike likePost(PostLikeRequest request) {
        Post post = getPostById(request.getPostId());
        User user = userService.getById(request.getUserId());

        // Kiểm tra user đã like chưa
        boolean alreadyLiked = false;
        for (PostLike like : post.getLikes()) {
            if (like.getUser().getId() == request.getUserId()) {
                alreadyLiked = true;
                break;
            }
        }
        if (alreadyLiked) {
            throw new RuntimeException("User đã like bài viết này rồi");
        }
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
        PostLike toRemove = null;
        for (PostLike like : post.getLikes()) {
            if (like.getUser().getId() == userId) {
                toRemove = like;
                break;
            }
        }

        if (toRemove == null) {
            throw new RuntimeException("User chưa like bài viết này");
        }

        post.getLikes().remove(toRemove);
        postRepository.save(post);
    }

    @Override
    public boolean isLiked(int postId, int userId) {
        Post post = getPostById(postId);
        for (PostLike like : post.getLikes()) {
            if (like.getUser().getId() == userId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PostLikeResponse> getLikesByPostId(int postId) {
        Post post = getPostById(postId);
        List<PostLikeResponse> responses = new ArrayList<>();
        for (PostLike like : post.getLikes()) {
            PostLikeResponse response = PostLikeResponse.builder()
                    .userName(like.getUser().getName())
                    .userAvatar(like.getUser().getAvatarUrl())
                    .build();
            responses.add(response);
        }
        return responses;
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

        // Nếu là reply - set parentCommentId
        if (request.getParentCommentId() != null) {
            // Kiểm tra parent cmt có tồn tại không
            boolean parentExists = false;
            for (PostComment c : post.getComments()) {
                if (c.getId() == request.getParentCommentId()) {
                    parentExists = true;
                    break;
                }
            }
            if (!parentExists) {
                throw new RuntimeException("Parent comment không tồn tại");
            }
            comment.setParentCommentId(request.getParentCommentId());
        } else {
            comment.setParentCommentId(0); //cmt gốc
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
            // Tìm và xóa replies của comment này
            List<PostComment> toRemove = new ArrayList<>();
            for (PostComment c : post.getComments()) {
                if (c.getParentCommentId() == commentId || c.getId() == commentId) {
                    toRemove.add(c);
                }
            }

            if (!toRemove.isEmpty()) {
                post.getComments().removeAll(toRemove);
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
                    return mapCommentToResponse(comment);
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
            PostCommentResponse res = mapCommentToResponse(c);
            responseList.add(res);
        }
        return responseList;
    }

    @Override
    public List<PostCommentResponse> getReplies(int parentCommentId) {
        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            // Kiểm tra parent comment có tồn tại không
            boolean hasParent = false;
            for (PostComment c : post.getComments()) {
                if (c.getId() == parentCommentId) {
                    hasParent = true;
                    break;
                }
            }

            if (hasParent) {
                List<PostCommentResponse> replies = new ArrayList<>();
                for (PostComment c : post.getComments()) {
                    if (c.getParentCommentId() == parentCommentId) {
                        PostCommentResponse res = mapCommentToResponse(c);
                        replies.add(res);
                    }
                }
                return replies;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public int countComments(int postId) {
        Post post = getPostById(postId);
        return post.getComments() != null ? post.getComments().size() : 0;
    }

    private PostCommentResponse mapCommentToResponse(PostComment comment) {
        PostCommentResponse response = new PostCommentResponse();
        response.setId(comment.getId());
        response.setUserName(comment.getUser().getName());
        response.setUserAvatar(comment.getUser().getAvatarUrl());
        response.setContent(comment.getContent());
        response.setParentCommentId(comment.getParentCommentId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}
