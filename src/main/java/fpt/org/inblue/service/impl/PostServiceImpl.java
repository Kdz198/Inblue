package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.enums.PostStatus;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.mapper.PostMapper;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.PostComment;
import fpt.org.inblue.model.PostLike;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.request.PostCommentRequest;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import fpt.org.inblue.model.dto.request.PostLikeRequest;
import fpt.org.inblue.model.dto.response.PostCommentResponse;
import fpt.org.inblue.model.dto.response.PostDetailResponse;
import fpt.org.inblue.model.dto.response.PostLikeResponse;
import fpt.org.inblue.model.dto.response.PostResponse;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.PostRepository;
import fpt.org.inblue.service.PostService;
import fpt.org.inblue.service.UserService;
import fpt.org.inblue.utils.SecurityUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final MentorRepository mentorRepository;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    // TODO OPTIMIZE - tránh n+1 query khi lấy post, like, comment SẼ LÀM SAU NÀY

    @Override
    public Post createPost(PostCreateRequest post) throws IOException {
        Actor actor = resolveCurrentActor();
        Map<String, String> uploadResult = cloudinaryService.uploadImg(post.getCoverImg());
        String url = uploadResult.get("secure_url");
        String public_id = uploadResult.get("public_id");
        Post saved = postMapper.toEntity(post);
        saved.setPublic_id(public_id);
        saved.setCoverImgUrl(url);
        saved.setAuthor(actor.user());
        saved.setAuthorMentor(actor.mentor());
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
                .author(mapPostAuthor(post))
                .build();
        response.setPost(detailResponse);
        response.setLikeCount(post.getLikes() != null ? post.getLikes().size() : 0);
        response.setCommentCount(post.getComments() != null ? post.getComments().size() : 0);
        List<PostLikeResponse> likeResponses = new ArrayList<>();
        if (post.getLikes() != null) {
            for (PostLike like : post.getLikes()) {
                likeResponses.add(mapLikeToResponse(like));
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
                    .author(mapPostAuthor(post))
                    .build();

            response.setPost(postDetail);
            response.setLikeCount(post.getLikes() != null ? post.getLikes().size() : 0);
            response.setCommentCount(
                    post.getComments() != null ? post.getComments().size() : 0);

            // map like
            List<PostLikeResponse> likeResponses = new ArrayList<>();
            if (post.getLikes() != null) {
                for (PostLike like : post.getLikes()) {
                    likeResponses.add(mapLikeToResponse(like));
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
    //    @Cacheable(value = "new_feed", key = "#page + '-' + #size")
    public Page<PostResponse> getNewFeed(int page, int size) {
        log.warn("🔥 CACHE MISS! Đang xuống PostgreSQL để lấy page: {}, size: {}", page, size);
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
        Actor actor = resolveCurrentActor();

        // Kiểm tra user đã like chưa
        boolean alreadyLiked = false;
        for (PostLike like : post.getLikes()) {
            if (isSameActor(like, actor)) {
                alreadyLiked = true;
                break;
            }
        }
        if (alreadyLiked) {
            throw new RuntimeException("User đã like bài viết này rồi");
        }
        PostLike postLike =
                PostLike.builder().user(actor.user()).mentor(actor.mentor()).build();

        post.getLikes().add(postLike);
        postRepository.save(post);

        return postLike;
    }

    @Override
    @Transactional
    public void unlikePost(int postId) {
        Post post = getPostById(postId);
        Actor actor = resolveCurrentActor();
        PostLike toRemove = null;
        for (PostLike like : post.getLikes()) {
            if (isSameActor(like, actor)) {
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
    public boolean isLiked(int postId) {
        Post post = getPostById(postId);
        Actor actor = resolveCurrentActor();
        for (PostLike like : post.getLikes()) {
            if (isSameActor(like, actor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public PostComment createComment(PostCommentRequest request) {
        Post post = getPostById(request.getPostId());
        Actor actor = resolveCurrentActor();

        PostComment comment = new PostComment();
        comment.setUser(actor.user());
        comment.setMentor(actor.mentor());
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
            comment.setParentCommentId(0); // cmt gốc
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

    private PostDetailResponse.AuthorResponse mapPostAuthor(Post post) {
        if (post.getAuthor() != null) {
            User user = post.getAuthor();
            return PostDetailResponse.AuthorResponse.builder()
                    .id(user.getId())
                    .role(Role.USER.name())
                    .name(user.getName())
                    .avatar(user.getAvatarUrl())
                    .build();
        }
        if (post.getAuthorMentor() != null) {
            Mentor mentor = post.getAuthorMentor();
            return PostDetailResponse.AuthorResponse.builder()
                    .id(mentor.getId())
                    .role(Role.MENTOR.name())
                    .name(mentor.getName())
                    .avatar(mentor.getAvatarUrl())
                    .build();
        }
        return null;
    }

    private PostLikeResponse mapLikeToResponse(PostLike like) {
        if (like.getUser() != null) {
            User user = like.getUser();
            return PostLikeResponse.builder()
                    .userId(user.getId())
                    .role(Role.USER.name())
                    .userName(user.getName())
                    .userAvatar(user.getAvatarUrl())
                    .build();
        }
        if (like.getMentor() != null) {
            Mentor mentor = like.getMentor();
            return PostLikeResponse.builder()
                    .userId(mentor.getId())
                    .role(Role.MENTOR.name())
                    .userName(mentor.getName())
                    .userAvatar(mentor.getAvatarUrl())
                    .build();
        }
        return PostLikeResponse.builder().build();
    }

    private PostCommentResponse mapCommentToResponse(PostComment comment) {
        PostCommentResponse response = new PostCommentResponse();
        response.setId(comment.getId());
        if (comment.getUser() != null) {
            response.setUserId(comment.getUser().getId());
            response.setRole(Role.USER.name());
            response.setUserName(comment.getUser().getName());
            response.setUserAvatar(comment.getUser().getAvatarUrl());
        } else if (comment.getMentor() != null) {
            response.setUserId(comment.getMentor().getId());
            response.setRole(Role.MENTOR.name());
            response.setUserName(comment.getMentor().getName());
            response.setUserAvatar(comment.getMentor().getAvatarUrl());
        }
        response.setContent(comment.getContent());
        response.setParentCommentId(comment.getParentCommentId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private Actor resolveCurrentActor() {
        return resolveActor(securityUtils.getCurrentUserId(), securityUtils.getCurrentRole());
    }

    private Actor resolveActor(int actorId, Role role) {
        if (role == Role.USER) {
            return new Actor(userService.getById(actorId), null);
        }
        if (role == Role.MENTOR) {
            Mentor mentor =
                    mentorRepository.findById(actorId).orElseThrow(() -> new RuntimeException("Mentor not found"));
            return new Actor(null, mentor);
        }
        throw new RuntimeException("Only USER or MENTOR can do this action");
    }

    private boolean isSameActor(PostLike like, Actor actor) {
        if (actor.user() != null) {
            return like.getUser() != null
                    && like.getUser().getId() == actor.user().getId();
        }
        if (actor.mentor() != null) {
            return like.getMentor() != null
                    && like.getMentor().getId().equals(actor.mentor().getId());
        }
        return false;
    }

    private record Actor(User user, Mentor mentor) {
        private Actor {
            if ((user == null && mentor == null) || (user != null && mentor != null)) {
                throw new RuntimeException("Invalid post actor");
            }
        }
    }
}
