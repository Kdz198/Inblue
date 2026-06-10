package fpt.org.inblue.repository;

import fpt.org.inblue.model.CodingProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodingProblemsRepository extends JpaRepository<CodingProblem, Long> {
    Optional<CodingProblem> findById(Long id);
}
