package fpt.org.inblue.entrytest.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.org.inblue.entrytest.dto.response.EntryTestAttemptResponse;
import fpt.org.inblue.entrytest.dto.response.EntryTestStartResponse;
import fpt.org.inblue.entrytest.model.EntryTest;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.repository.EntryTestAttemptRepository;
import fpt.org.inblue.entrytest.repository.EntryTestRepository;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.entrytest.service.UserCompetencyService;
import fpt.org.inblue.mapper.EntryTestResponseMapper;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.repository.QuestionBankRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.ApiClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntryTestAssociationTest {
    @Mock
    private EntryTestRepository entryTestRepository;

    @Mock
    private EntryTestAttemptRepository attemptRepository;

    @Mock
    private UserCareerPreferenceRepository preferenceRepository;

    @Mock
    private QuestionBankRepository questionBankRepository;

    @Mock
    private CodingProblemsRepository codingProblemsRepository;

    @Mock
    private ApiClient apiClient;

    @Mock
    private UserCompetencyService userCompetencyService;

    @Mock
    private UserRepository userRepository;

    private EntryTestResponseMapper responseMapper;
    private EntryTestServiceImpl service;

    @BeforeEach
    void setUp() {
        responseMapper = Mappers.getMapper(EntryTestResponseMapper.class);
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
    void startEntryTestMaintainsBothSidesOfUserAttemptAssociation() {
        User user = User.builder().id(7).entryTestAttempts(new java.util.ArrayList<>()).build();
        UserCareerPreference preference = UserCareerPreference.builder()
                .userId(7)
                .languagesJson(List.of("JAVA"))
                .isActive(true)
                .build();
        EntryTest entryTest = EntryTest.builder()
                .id(3L)
                .timeLimitMinutes(60)
                .sectionConfigs(List.of())
                .isActive(true)
                .build();

        when(preferenceRepository.findByUserIdAndIsActiveTrue(7)).thenReturn(Optional.of(preference));
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(entryTestRepository.findFirstByIsActiveTrueOrderByUpdatedAtDesc()).thenReturn(Optional.of(entryTest));
        when(attemptRepository.save(any(EntryTestAttempt.class))).thenAnswer(invocation -> {
            EntryTestAttempt attempt = invocation.getArgument(0);
            attempt.setId(12L);
            return attempt;
        });

        EntryTestStartResponse response = service.startEntryTest(7);

        assertEquals(12L, response.getAttemptId());
        assertEquals(1, user.getEntryTestAttempts().size());
        assertSame(user, user.getEntryTestAttempts().getFirst().getUser());
    }

    @Test
    void responseMapperKeepsUserIdWithoutSerializingUserOrCorrectAnswer() throws Exception {
        User user = User.builder().id(7).build();
        EntryTestAttempt attempt = EntryTestAttempt.builder()
                .id(12L)
                .user(user)
                .commonQuizItemsJson(List.of(EntryTestAttempt.QuestionItemSnapshot.builder()
                        .itemId("COMMON-1")
                        .questionText("Question")
                        .correctAnswer("A")
                        .build()))
                .build();

        EntryTestAttemptResponse response = responseMapper.toAttemptResponse(attempt);
        String json = new ObjectMapper().writeValueAsString(response);

        assertEquals(7, response.getUserId());
        assertFalse(json.contains("\"user\""));
        assertFalse(json.contains("correctAnswer"));
        assertFalse(json.contains("\"A\""));
    }

    @Test
    void removeEntryTestAttemptMaintainsBothSides() {
        User user = User.builder().id(7).entryTestAttempts(new java.util.ArrayList<>()).build();
        EntryTestAttempt attempt = EntryTestAttempt.builder().build();
        user.addEntryTestAttempt(attempt);

        user.removeEntryTestAttempt(attempt);

        assertEquals(0, user.getEntryTestAttempts().size());
        assertNull(attempt.getUser());
    }
}
