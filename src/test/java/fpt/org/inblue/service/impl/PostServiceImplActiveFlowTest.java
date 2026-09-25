package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.enums.PostStatus;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.mapper.PostMapper;
import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.PostComment;
import fpt.org.inblue.model.PostLike;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.request.PostCommentRequest;
import fpt.org.inblue.model.dto.request.PostLikeRequest;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.PostRepository;
import fpt.org.inblue.service.UserService;
import fpt.org.inblue.utils.SecurityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceImplActiveFlowTest {
    @Mock
    PostMapper mapper;

    @Mock
    PostRepository repository;

    @Mock
    MentorRepository mentorRepository;

    @Mock
    CloudinaryService cloudinaryService;

    @Mock
    UserService userService;

    @Mock
    SecurityUtils securityUtils;

    private PostServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PostServiceImpl(
                mapper, repository, mentorRepository, cloudinaryService, userService, securityUtils);
    }

    @Test
    void getPostByIdRejectsUnknownPost() {
        when(repository.findById(9)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getPostById(9));
    }

    @Test
    void changeStatusPersistsPublishedPost() {
        Post post = post();
        when(repository.existsById(1)).thenReturn(true);
        when(repository.findById(1)).thenReturn(Optional.of(post));
        service.changeStatus(1, PostStatus.PUBLISHED);
        assertEquals(PostStatus.PUBLISHED, post.getStatus());
        verify(repository).save(post);
    }

    @Test
    void publishedQueryMapsEmptyCollection() {
        when(repository.findAllByStatus(PostStatus.PUBLISHED)).thenReturn(List.of());
        assertEquals(List.of(), service.getPublishPost());
    }

    @Test
    void likePostRejectsDuplicateActor() {
        User user = User.builder().id(7).build();
        Post post = post();
        post.getLikes().add(PostLike.builder().user(user).build());
        currentUser(user);
        when(repository.findById(1)).thenReturn(Optional.of(post));
        PostLikeRequest request = new PostLikeRequest();
        request.setPostId(1);
        assertThrows(RuntimeException.class, () -> service.likePost(request));
    }

    @Test
    void likeAndUnlikePostUpdateCollection() {
        User user = User.builder().id(7).build();
        Post post = post();
        currentUser(user);
        when(repository.findById(1)).thenReturn(Optional.of(post));
        PostLikeRequest request = new PostLikeRequest();
        request.setPostId(1);
        service.likePost(request);
        assertTrue(service.isLiked(1));
        service.unlikePost(1);
        assertFalse(service.isLiked(1));
    }

    @Test
    void createReplyRejectsMissingParentComment() {
        User user = User.builder().id(7).build();
        Post post = post();
        currentUser(user);
        when(repository.findById(1)).thenReturn(Optional.of(post));
        PostCommentRequest request = new PostCommentRequest();
        request.setPostId(1);
        request.setContent("reply");
        request.setParentCommentId(99);
        assertThrows(RuntimeException.class, () -> service.createComment(request));
    }

    @Test
    void deleteCommentRemovesCommentAndReplies() {
        Post post = post();
        PostComment root = new PostComment();
        root.setId(1);
        root.setParentCommentId(0);
        PostComment reply = new PostComment();
        reply.setId(2);
        reply.setParentCommentId(1);
        post.setComments(new ArrayList<>(List.of(root, reply)));
        when(repository.findAll()).thenReturn(List.of(post));
        service.deleteComment(1);
        assertTrue(post.getComments().isEmpty());
    }

    private Post post() {
        Post post = new Post();
        post.setPostId(1);
        post.setLikes(new ArrayList<>());
        post.setComments(new ArrayList<>());
        return post;
    }

    private void currentUser(User user) {
        when(securityUtils.getCurrentUserId()).thenReturn(user.getId());
        when(securityUtils.getCurrentRole()).thenReturn(Role.USER);
        when(userService.getById(user.getId())).thenReturn(user);
    }
}
