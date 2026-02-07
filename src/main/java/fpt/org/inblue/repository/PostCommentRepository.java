package fpt.org.inblue.repository;

import fpt.org.inblue.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {
    List<PostComment> findAllByPostPostIdAndParentCommentIsNullOrderByCreatedAtDesc(int postId);

    List<PostComment> findAllByParentCommentIdOrderByCreatedAtAsc(int parentId);

    List<PostComment> findAllByPostPostIdOrderByCreatedAtDesc(int postId);

    int countByPostPostId(int postId);
}

