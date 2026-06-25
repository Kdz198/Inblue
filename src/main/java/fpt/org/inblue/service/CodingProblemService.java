package fpt.org.inblue.service;

import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.model.dto.request.CodingProblemGenerateRequest;
import fpt.org.inblue.model.dto.response.CodingProblemGenerateResponse;
import java.util.List;
import java.util.Optional;

public interface CodingProblemService {
    Optional<CodingProblem> findCodingProblemById(Long id);

    CodingProblem save(CodingProblem codingProblem);

    List<CodingProblem> findAllCodingProblems();

    CodingProblemGenerateResponse generateCodingProblem(CodingProblemGenerateRequest codingProblemGenerateRequest);
}
