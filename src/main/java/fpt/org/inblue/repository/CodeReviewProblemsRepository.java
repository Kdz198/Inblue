package fpt.org.inblue.repository;

import fpt.org.inblue.model.CodeReviewProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeReviewProblemsRepository extends JpaRepository<CodeReviewProblem, Long> {
    Optional<CodeReviewProblem> findById(Long id);
}
