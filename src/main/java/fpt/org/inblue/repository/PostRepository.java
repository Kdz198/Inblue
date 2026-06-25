package fpt.org.inblue.repository;

import fpt.org.inblue.enums.PostStatus;
import fpt.org.inblue.model.Post;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findAllByStatus(PostStatus status);

    Page<Post> findAllByOrderByCreationDateDesc(Pageable pageable);
}
