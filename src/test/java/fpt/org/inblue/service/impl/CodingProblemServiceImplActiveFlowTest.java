package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.model.dto.request.CodingProblemGenerateRequest;
import fpt.org.inblue.model.dto.response.CodingProblemGenerateResponse;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.service.ApiClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodingProblemServiceImplActiveFlowTest {
    @Mock
    CodingProblemsRepository repository;

    @Mock
    ApiClient apiClient;

    private CodingProblemServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CodingProblemServiceImpl(repository, apiClient);
    }

    @Test
    void findCodingProblemByIdReturnsRepositoryOptional() {
        CodingProblem problem = CodingProblem.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(problem));
        assertEquals(Optional.of(problem), service.findCodingProblemById(1L));
    }

    @Test
    void findCodingProblemByIdPreservesEmptyResult() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), service.findCodingProblemById(99L));
    }

    @Test
    void saveDelegatesEntityToRepository() {
        CodingProblem problem = CodingProblem.builder().title("Two Sum").build();
        when(repository.save(problem)).thenReturn(problem);
        assertSame(problem, service.save(problem));
    }

    @Test
    void findAllCodingProblemsPreservesEmptyList() {
        when(repository.findAll()).thenReturn(List.of());
        assertEquals(List.of(), service.findAllCodingProblems());
    }

    @Test
    void generateCodingProblemUsesIndependentCodingWorkspace() {
        CodingProblemGenerateRequest request = new CodingProblemGenerateRequest();
        CodingProblemGenerateResponse response =
                CodingProblemGenerateResponse.builder().title("Generated").build();
        when(apiClient.sendChatToAnythingLlm(
                        AnythingLlmWorkspace.CODING_GEN,
                        request,
                        "java",
                        true,
                        null,
                        CodingProblemGenerateResponse.class))
                .thenReturn(response);
        assertSame(response, service.generateCodingProblem(request));
        verify(apiClient)
                .sendChatToAnythingLlm(
                        AnythingLlmWorkspace.CODING_GEN,
                        request,
                        "java",
                        true,
                        null,
                        CodingProblemGenerateResponse.class);
    }
}
