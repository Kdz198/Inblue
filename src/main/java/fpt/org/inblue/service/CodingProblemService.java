package fpt.org.inblue.service;

import fpt.org.inblue.model.CodingProblem;

import java.util.List;
import java.util.Optional;

public interface CodingProblemService {
    Optional<CodingProblem> findCodingProblemById(Long id);
    CodingProblem save(CodingProblem codingProblem);
    List<CodingProblem> findAllCodingProblems();
}
