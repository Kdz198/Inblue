package fpt.org.inblue.entrytest.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.entrytest.dto.request.EntryTestRunCodeRequest;
import fpt.org.inblue.entrytest.dto.request.EntryTestSubmitRequest;
import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.entrytest.model.EntryTest;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.repository.EntryTestAttemptRepository;
import fpt.org.inblue.entrytest.repository.EntryTestRepository;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.service.UserCompetencyService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.EntryTestResponseMapper;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.repository.QuestionBankRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.ApiClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntryTestServiceImplActiveFlowTest {
    @Mock
    EntryTestRepository entryTestRepository;

    @Mock
    EntryTestAttemptRepository attemptRepository;

    @Mock
    UserCareerPreferenceRepository preferenceRepository;

    @Mock
    QuestionBankRepository questionBankRepository;

    @Mock
    CodingProblemsRepository codingProblemsRepository;

    @Mock
    ApiClient apiClient;

    @Mock
    UserCompetencyService userCompetencyService;

    @Mock
    UserRepository userRepository;

    @Mock
    EntryTestResponseMapper responseMapper;

    private EntryTestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EntryTestServiceImpl(
                entryTestRepository,
                attemptRepository,
                preferenceRepository,
                questionBankRepository,
                codingProblemsRepository,
                apiClient,
                userCompetencyService,
                userRepository,
                responseMapper);
    }

    @Test
    void startEntryTestRejectsMissingCareerPreference() {
        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.startEntryTest(7));
        assertEquals(400, error.getStatus().value());
    }

    @Test
    void startEntryTestRejectsMissingUser() {
        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.of(preference()));
        when(userRepository.findById(7)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.startEntryTest(7));
        assertEquals(404, error.getStatus().value());
    }

    @Test
    void startEntryTestCreatesAttemptAndMaintainsUserAssociation() {
        User user = User.builder()
                .id(7)
                .entryTestAttempts(new java.util.ArrayList<>())
                .build();
        EntryTest entryTest = EntryTest.builder()
                .id(3L)
                .timeLimitMinutes(60)
                .sectionConfigs(List.of())
                .isActive(true)
                .build();
        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.of(preference()));
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(entryTestRepository.findFirstByIsActiveTrueOrderByUpdatedAtDesc()).thenReturn(Optional.of(entryTest));
        when(attemptRepository.save(org.mockito.ArgumentMatchers.any(EntryTestAttempt.class)))
                .thenAnswer(invocation -> {
                    EntryTestAttempt attempt = invocation.getArgument(0);
                    attempt.setId(12L);
                    return attempt;
                });

        var response = service.startEntryTest(7);

        assertEquals(12L, response.getAttemptId());
        assertEquals(60, response.getTimeLimitMinutes());
        assertEquals(1, user.getEntryTestAttempts().size());
        assertSame(user, user.getEntryTestAttempts().getFirst().getUser());
    }

    @Test
    void getAttemptRejectsUnknownAttempt() {
        when(attemptRepository.findById(99L)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.getAttempt(7, 99L));
        assertEquals(404, error.getStatus().value());
    }

    @Test
    void getAttemptForbidsAnotherUsersAttempt() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.IN_PROGRESS);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        CustomException error = assertThrows(CustomException.class, () -> service.getAttempt(8, 12L));
        assertEquals(403, error.getStatus().value());
    }

    @Test
    void runCodeRejectsSubmittedAttempt() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.GRADED);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        CustomException error = assertThrows(
                CustomException.class,
                () -> service.runCode(7, 12L, runRequest("CODING-1", "JAVA", List.of("return 1;"))));
        assertEquals(400, error.getStatus().value());
    }

    @Test
    void runCodeRejectsItemOutsideAttempt() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.IN_PROGRESS);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        CustomException error = assertThrows(
                CustomException.class,
                () -> service.runCode(7, 12L, runRequest("OTHER", "JAVA", List.of("return 1;"))));
        assertEquals(400, error.getStatus().value());
    }

    @Test
    void runCodeRejectsEmptySourceCode() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.IN_PROGRESS);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        CustomException error = assertThrows(
                CustomException.class, () -> service.runCode(7, 12L, runRequest("CODING-1", "JAVA", List.of(" "))));
        assertEquals(400, error.getStatus().value());
        verify(apiClient, never()).executeCode(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void runCodeRejectsUnsupportedLanguage() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.IN_PROGRESS);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        CustomException error = assertThrows(
                CustomException.class,
                () -> service.runCode(7, 12L, runRequest("CODING-1", "BRAINFUCK", List.of("+"))));
        assertEquals(400, error.getStatus().value());
    }

    @Test
    void runCodeReturnsCompilerResponse() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.IN_PROGRESS);
        CompilerResponseDto response = new CompilerResponseDto();
        response.setTotalTestCases(2);
        response.setPassedTestCases(2);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        when(apiClient.executeCode(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        assertSame(response, service.runCode(7, 12L, runRequest("CODING-1", "JavaScript", List.of("return 1;"))));
    }

    @Test
    void runCodeRejectsNullCompilerResponse() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.IN_PROGRESS);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        when(apiClient.executeCode(org.mockito.ArgumentMatchers.any())).thenReturn(null);
        CustomException error = assertThrows(
                CustomException.class,
                () -> service.runCode(7, 12L, runRequest("CODING-1", "JAVA", List.of("return 1;"))));
        assertEquals(502, error.getStatus().value());
    }

    @Test
    void submitRejectsDuplicateItemIds() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.IN_PROGRESS);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        EntryTestSubmitRequest.Answer answer = EntryTestSubmitRequest.Answer.builder()
                .itemId("COMMON-1")
                .answerJson(Map.of("selectedOption", "A"))
                .build();
        EntryTestSubmitRequest request = EntryTestSubmitRequest.builder()
                .answers(List.of(answer, answer))
                .build();

        CustomException error = assertThrows(CustomException.class, () -> service.submitEntryTest(7, 12L, request));
        assertEquals(400, error.getStatus().value());
    }

    @Test
    void submitGradesQuestionAndUpdatesCompetency() {
        EntryTestAttempt attempt = attempt(EntryTestAttempt.AttemptStatus.IN_PROGRESS);
        when(attemptRepository.findById(12L)).thenReturn(Optional.of(attempt));
        when(userCompetencyService.resolveLevelName(attempt)).thenReturn("JUNIOR");
        when(attemptRepository.save(attempt)).thenReturn(attempt);
        EntryTestSubmitRequest request = EntryTestSubmitRequest.builder()
                .answers(List.of(EntryTestSubmitRequest.Answer.builder()
                        .itemId("COMMON-1")
                        .answerJson(Map.of("selectedOption", "a. first"))
                        .build()))
                .build();

        EntryTestAttempt result = service.submitEntryTest(7, 12L, request);

        assertEquals(10.0, result.getCommonQuizScore());
        assertEquals(10.0, result.getFinalScore());
        assertEquals(EntryTestAttempt.AttemptStatus.GRADED, result.getStatus());
        assertEquals("JUNIOR", result.getResultLevel());
        verify(userCompetencyService).updateAfterEntryTest(attempt);
    }

    private UserCareerPreference preference() {
        return UserCareerPreference.builder()
                .userId(7)
                .targetRole(TargetRole.BE)
                .skills(List.of("Java"))
                .build();
    }

    private EntryTestAttempt attempt(EntryTestAttempt.AttemptStatus status) {
        return EntryTestAttempt.builder()
                .id(12L)
                .user(User.builder().id(7).build())
                .status(status)
                .commonQuizItemsJson(List.of(EntryTestAttempt.QuestionItemSnapshot.builder()
                        .itemId("COMMON-1")
                        .correctAnswer("A")
                        .maxScore(10.0)
                        .build()))
                .specificQuizItemsJson(List.of())
                .specificCodingItemsJson(List.of(EntryTestAttempt.CodingProblemItemSnapshot.builder()
                        .itemId("CODING-1")
                        .codingProblemId(5L)
                        .maxScore(20.0)
                        .visibleExamples(List.of())
                        .build()))
                .build();
    }

    private EntryTestRunCodeRequest runRequest(String itemId, String language, List<String> source) {
        return EntryTestRunCodeRequest.builder()
                .itemId(itemId)
                .language(language)
                .sourceCode(source)
                .build();
    }
}
