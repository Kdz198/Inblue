package fpt.org.inblue.repository;

import fpt.org.inblue.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {
    List<PostLike> findAllByPostPostId(int postId);

    Optional<PostLike> findByPostPostIdAndUserId(int postId, int userId);

    boolean existsByPostPostIdAndUserId(int postId, int userId);

    void deleteByPostPostIdAndUserId(int postId, int userId);

    int countByPostPostId(int postId);
}

