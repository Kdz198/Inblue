//package fpt.org.inblue.entrytest.service.impl;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import fpt.org.inblue.entrytest.dto.response.UserCompetencyResponse;
//import fpt.org.inblue.entrytest.enums.TargetRole;
//import fpt.org.inblue.entrytest.model.EntryTestAttempt;
//import fpt.org.inblue.entrytest.model.LevelScale;
//import fpt.org.inblue.entrytest.model.UserCareerPreference;
//import fpt.org.inblue.entrytest.model.UserCompetency;
//import fpt.org.inblue.entrytest.repository.LevelScaleRepository;
//import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
//import fpt.org.inblue.entrytest.repository.UserCompetencyRepository;
//import fpt.org.inblue.enums.TargetLevel;
//import fpt.org.inblue.mapper.UserCompetencyMapper;
//import fpt.org.inblue.model.User;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mapstruct.factory.Mappers;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class UserCompetencyAssociationTest {
//    @Mock
//    private UserCompetencyRepository competencyRepository;
//
//    @Mock
//    private UserCareerPreferenceRepository preferenceRepository;
//
//    @Mock
//    private LevelScaleRepository levelScaleRepository;
//
//    private UserCompetencyServiceImpl service;
//    private UserCompetencyMapper mapper;
//
//    @BeforeEach
//    void setUp() {
//        service = new UserCompetencyServiceImpl(competencyRepository, preferenceRepository, levelScaleRepository);
//        mapper = Mappers.getMapper(UserCompetencyMapper.class);
//    }
//
//    @Test
//    void updateAfterEntryTestCreatesCompetencyAroundAttemptUser() {
//        User user = User.builder().id(7).build();
//        EntryTestAttempt attempt = EntryTestAttempt.builder()
//                .id(12L)
//                .user(user)
//                .careerPreferenceId(7)
//                .finalScore(80.0)
//                .specificCodingScore(30.0)
//                .build();
//        UserCareerPreference preference = UserCareerPreference.builder()
//                .userId(7)
//                .targetRole(TargetRole.BE)
//                .languagesJson(List.of("JAVA"))
//                .build();
//        LevelScale levelScale = LevelScale.builder()
//                .targetRole(TargetRole.BE)
//                .level(TargetLevel.JUNIOR)
//                .minScore(70.0)
//                .maxScore(89.99)
//                .minCodingScore(20.0)
//                .build();
//
//        when(preferenceRepository.findById(7)).thenReturn(Optional.of(preference));
//        when(levelScaleRepository.findAllByIsActiveTrue()).thenReturn(List.of(levelScale));
//        when(competencyRepository.findByUser_IdAndCareerPreferenceId(7, 7)).thenReturn(Optional.empty());
//        when(competencyRepository.save(any(UserCompetency.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserCompetency result = service.updateAfterEntryTest(attempt);
//
//        assertSame(user, result.getUser());
//        assertEquals(TargetLevel.JUNIOR, result.getCurrentLevel());
//        verify(preferenceRepository).save(preference);
//    }
//
//    @Test
//    void mapperKeepsUserIdWithoutSerializingUserGraph() throws Exception {
//        UserCompetency competency = UserCompetency.builder()
//                .id(5L)
//                .user(User.builder().id(7).build())
//                .currentLevel(TargetLevel.INTERN)
//                .build();
//
//        UserCompetencyResponse response = mapper.toResponse(competency);
//        String json = new ObjectMapper().writeValueAsString(response);
//
//        assertEquals(7, response.getUserId());
//        assertFalse(json.contains("\"user\""));
//    }
//
//    @Test
//    void repositoryLookupUsesUserAssociationId() {
//        when(competencyRepository.findFirstByUser_IdOrderByUpdatedAtDesc(7))
//                .thenReturn(Optional.of(UserCompetency.builder()
//                        .user(User.builder().id(7).build())
//                        .build()));
//
//        UserCompetency result = service.getCurrentCompetency(7);
//
//        assertEquals(7, result.getUser().getId());
//    }
//}
