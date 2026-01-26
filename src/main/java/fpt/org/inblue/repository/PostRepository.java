package fpt.org.inblue.repository;

import fpt.org.inblue.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {
}
