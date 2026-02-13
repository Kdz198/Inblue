package fpt.org.inblue.controller;

import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.PostComment;
import fpt.org.inblue.model.PostLike;
import fpt.org.inblue.model.dto.request.PostCommentRequest;
import fpt.org.inblue.model.dto.request.PostCreateRequest;
import fpt.org.inblue.model.dto.request.PostLikeRequest;
import fpt.org.inblue.model.dto.response.PostCommentResponse;
import fpt.org.inblue.model.dto.response.PostLikeResponse;
import fpt.org.inblue.model.enums.PostStatus;
import fpt.org.inblue.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin("*")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create post",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PostCreateRequest.class)
                    )
            )
    )
    public ResponseEntity<Post> createPost(@ModelAttribute PostCreateRequest postCreateRequest) throws IOException {
        return ResponseEntity.ok().body(postService.createPost(postCreateRequest));
    }



    @PostMapping("/likes")
    @Operation(summary = "Like bài viết", description = "User like một bài viết, truyền postId và userId")
    public ResponseEntity<PostLike> likePost(@RequestBody PostLikeRequest request) {
        return ResponseEntity.ok(postService.likePost(request));
    }

    @DeleteMapping("/likes/{postId}/{userId}")
    @Operation(summary = "Unlike bài viết", description = "User unlike một bài viết")
    public ResponseEntity<Map<String, String>> unlikePost(@PathVariable int postId, @PathVariable int userId) {
        postService.unlikePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Unlike thành công"));
    }

    @GetMapping("/likes/{postId}")
    @Operation(summary = "Lấy danh sách người đã like bài viết")
    public ResponseEntity<List<PostLikeResponse>> getLikesByPostId(@PathVariable int postId) {
        return ResponseEntity.ok(postService.getLikesByPostId(postId));
    }

    @GetMapping("/likes/{postId}/count")
    @Operation(summary = "Đếm số lượng like của bài viết")
    public ResponseEntity<Integer> countLikes(@PathVariable int postId) {
        return ResponseEntity.ok( postService.countLikes(postId));
    }

    @GetMapping("/likes/{postId}/check/{userId}")
    @Operation(summary = "Kiểm tra user đã like bài viết chưa")
    public ResponseEntity<Map<String, Boolean>> checkLiked(@PathVariable int postId, @PathVariable int userId) {
        return ResponseEntity.ok(Map.of("isLiked", postService.isLiked(postId, userId)));
    }


    @PostMapping("/comments")
    @Operation(summary = "Tạo comment mới",
            description = "Tạo comment cho bài viết. Nếu là reply thì truyền parentCommentId, nếu là comment gốc thì parentCommentId = null")
    public ResponseEntity<PostComment> createComment(@RequestBody PostCommentRequest request) {
        return ResponseEntity.ok(postService.createComment(request));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Cập nhật nội dung comment")
    public ResponseEntity<PostComment> updateComment(@PathVariable int commentId, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        return ResponseEntity.ok(postService.updateComment(commentId, content));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Xóa comment")
    public ResponseEntity<Void> deleteComment(@PathVariable int commentId) {
        postService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comments/{commentId}")
    @Operation(summary = "Lấy chi tiết comment theo id")
    public ResponseEntity<PostCommentResponse> getCommentById(@PathVariable int commentId) {
        return ResponseEntity.ok(postService.getCommentById(commentId));
    }

    @GetMapping("/{postId}/comments")
    @Operation(summary = "Lấy tất cả comments của bài viết",
            description = "Trả về danh sách comments gốc kèm theo replies")
    public ResponseEntity<List<PostCommentResponse>> getCommentsByPostId(@PathVariable int postId) {
        return ResponseEntity.ok(postService.getCommentsByPostId(postId));
    }

    @GetMapping("/comments/{parentCommentId}/replies")
    @Operation(summary = "Lấy replies của một comment")
    public ResponseEntity<List<PostCommentResponse>> getReplies(@PathVariable int parentCommentId) {
        return ResponseEntity.ok(postService.getReplies(parentCommentId));
    }

    @GetMapping("/{postId}/comments/count")
    @Operation(summary = "Đếm số lượng comments của bài viết")
    public ResponseEntity<Integer> countComments(@PathVariable int postId) {
        return ResponseEntity.ok( postService.countComments(postId));
    }
    @GetMapping
    @Operation(summary = "Lấy tất cả bài viết")
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPost());}

    @GetMapping("/published")
    @Operation(summary = "Lấy tất cả bài viết đã publish")
    public ResponseEntity<List<Post>> getPublishedPosts() {
        return ResponseEntity.ok(postService.getPublishPost());}

    @GetMapping("/change-status/{postId}")
    @Operation(summary = "Thay đổi trạng thái bài viết")
    public ResponseEntity<Map<String, String>> changeStatus(@PathVariable int postId, @RequestParam PostStatus status) {
        postService.changeStatus(postId, status);
        return ResponseEntity.ok(Map.of("message", "Thay đổi trạng thái thành công"));}
}
