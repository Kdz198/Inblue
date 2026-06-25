package fpt.org.inblue.service;

import fpt.org.inblue.model.CodeReviewProblem;
import fpt.org.inblue.model.dto.request.CodeReviewProblemGenerateRequest;
import fpt.org.inblue.model.dto.response.CodeReviewProblemGenerateResponse;
import java.util.List;
import java.util.Optional;

public interface CodeReviewProblemService {
    Optional<CodeReviewProblem> findCodeReviewProblemById(Long id);

    CodeReviewProblem save(CodeReviewProblem codeReviewProblem);

    List<CodeReviewProblem> findAllCodeReviewProblems();

    CodeReviewProblemGenerateResponse generateCodeReviewProblem(CodeReviewProblemGenerateRequest request);
}
