package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.model.CodeReviewProblem;
import fpt.org.inblue.model.dto.request.CodeReviewProblemGenerateRequest;
import fpt.org.inblue.model.dto.response.CodeReviewProblemGenerateResponse;
import fpt.org.inblue.repository.CodeReviewProblemsRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.CodeReviewProblemService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodeReviewProblemServiceImpl implements CodeReviewProblemService {

    private final CodeReviewProblemsRepository codeReviewProblemsRepository;
    private final ApiClient apiClient;

    @Override
    public Optional<CodeReviewProblem> findCodeReviewProblemById(Long id) {
        return codeReviewProblemsRepository.findById(id);
    }

    @Override
    public CodeReviewProblem save(CodeReviewProblem codeReviewProblem) {
        codeReviewProblem.setIsDeleted(false);
        return codeReviewProblemsRepository.save(codeReviewProblem);
    }

    @Override
    public List<CodeReviewProblem> findAllCodeReviewProblems() {
        return codeReviewProblemsRepository.findAll();
    }

    @Override
    public CodeReviewProblemGenerateResponse generateCodeReviewProblem(CodeReviewProblemGenerateRequest request) {
        CodeReviewProblemGenerateResponse response = apiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.CODE_REVIEW_GEN,
                request,
                "java-code-review", // sessionId
                true, // reset session to ensure independent calls
                null, // no attached files
                CodeReviewProblemGenerateResponse.class);
        System.out.println("Received Code Review response from LLM: " + response);
        return response;
    }
}
