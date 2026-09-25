package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.model.CodeReviewProblem;
import fpt.org.inblue.model.dto.request.CodeReviewProblemGenerateRequest;
import fpt.org.inblue.model.dto.response.CodeReviewProblemGenerateResponse;
import fpt.org.inblue.repository.CodeReviewProblemsRepository;
import fpt.org.inblue.service.ApiClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeReviewProblemServiceImplActiveFlowTest {
    @Mock
    CodeReviewProblemsRepository repository;

    @Mock
    ApiClient apiClient;

    private CodeReviewProblemServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CodeReviewProblemServiceImpl(repository, apiClient);
    }

    @Test
    void findCodeReviewProblemByIdReturnsRepositoryOptional() {
        CodeReviewProblem problem = CodeReviewProblem.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(problem));
        assertEquals(Optional.of(problem), service.findCodeReviewProblemById(1L));
    }

    @Test
    void findCodeReviewProblemByIdPreservesEmptyResult() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), service.findCodeReviewProblemById(99L));
    }

    @Test
    void saveReactivatesSoftDeletedProblem() {
        CodeReviewProblem problem = CodeReviewProblem.builder().isDeleted(true).build();
        when(repository.save(problem)).thenReturn(problem);
        CodeReviewProblem result = service.save(problem);
        assertFalse(result.getIsDeleted());
        verify(repository).save(problem);
    }

    @Test
    void findAllCodeReviewProblemsReturnsRepositoryData() {
        CodeReviewProblem problem = CodeReviewProblem.builder().id(1L).build();
        when(repository.findAll()).thenReturn(List.of(problem));
        assertEquals(List.of(problem), service.findAllCodeReviewProblems());
    }

    @Test
    void generateCodeReviewProblemUsesIndependentReviewWorkspace() {
        CodeReviewProblemGenerateRequest request = new CodeReviewProblemGenerateRequest();
        CodeReviewProblemGenerateResponse response =
                CodeReviewProblemGenerateResponse.builder().title("Review").build();
        when(apiClient.sendChatToAnythingLlm(
                        AnythingLlmWorkspace.CODE_REVIEW_GEN,
                        request,
                        "java-code-review",
                        true,
                        null,
                        CodeReviewProblemGenerateResponse.class))
                .thenReturn(response);
        assertSame(response, service.generateCodeReviewProblem(request));
    }
}
