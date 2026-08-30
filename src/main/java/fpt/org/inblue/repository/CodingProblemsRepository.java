package fpt.org.inblue.repository;

import fpt.org.inblue.model.CodingProblem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingProblemsRepository extends JpaRepository<CodingProblem, Long> {
    Optional<CodingProblem> findById(Long id);

    List<CodingProblem> findAllByIsDeletedFalse();
}
