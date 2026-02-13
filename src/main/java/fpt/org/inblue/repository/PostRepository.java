package fpt.org.inblue.repository;

import fpt.org.inblue.model.Post;
import fpt.org.inblue.model.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findAllByStatus(PostStatus status);

}
