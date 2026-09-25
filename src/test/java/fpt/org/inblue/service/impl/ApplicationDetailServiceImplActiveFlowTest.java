package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.CandidateProfileRepository;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.RoundRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.security.JwtUtils;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.InterviewSessionService;
import fpt.org.inblue.service.MentorService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationDetailServiceImplActiveFlowTest {
    @Mock
    ApplicationDetailRepository detailRepository;

    @Mock
    ApplicationService applicationService;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    RoundRepository roundRepository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CandidateProfileRepository profileRepository;

    @Mock
    InterviewSessionService interviewSessionService;

    @Mock
    InterviewSessionRepository interviewSessionRepository;

    @Mock
    MentorService mentorService;

    @Mock
    fpt.org.inblue.service.SessionService sessionService;

    @Mock
    fpt.org.inblue.repository.MentorRepository mentorRepository;

    @Mock
    JwtUtils jwtUtils;

    private ApplicationDetailServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApplicationDetailServiceImpl(
                detailRepository,
                applicationRepository,
                applicationService,
                roundRepository,
                jobRepository,
                userRepository,
                profileRepository,
                interviewSessionService,
                interviewSessionRepository,
                mentorService,
                mentorRepository,
                sessionService,
                jwtUtils);
    }

    @Test
    void getApplicationDetailByIdRejectsUnknownDetail() {
        when(detailRepository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getApplicationDetailById(9L))
                        .getStatus()
                        .value());
    }

    @Test
    void getByApplicationIdDelegatesToRepository() {
        ApplicationDetail detail = ApplicationDetail.builder().id(1L).build();
        when(detailRepository.findAllByApplicationId(4L)).thenReturn(List.of(detail));
        assertEquals(List.of(detail), service.getByApplicationId(4L));
    }

    @Test
    void hrScoreCompletesDetailAndAdvancesApplication() {
        ApplicationDetail detail =
                ApplicationDetail.builder().id(1L).applicationId(4L).build();
        Application application = Application.builder().id(4L).build();
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(applicationService.getApplicationById(4L)).thenReturn(application);
        service.hrScore(1L, true, "Strong", 8.5);
        assertEquals(ApplicationDetailStatus.COMPLETED, detail.getStatus());
        assertEquals(ApplicationDetail.RoundResult.PASSED, detail.getFinalResult());
        assertEquals(8.5, detail.getFinalScore());
        verify(applicationService).moveToNextRound(application);
    }

    @Test
    void assignMentorRejectsInvalidStatus() {
        ApplicationDetail detail = ApplicationDetail.builder()
                .id(1L)
                .status(ApplicationDetailStatus.COMPLETED)
                .build();
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.assignMentor(1L, 3))
                        .getStatus()
                        .value());
    }

    @Test
    void assignMentorsRejectsEmptyList() {
        ApplicationDetail detail = ApplicationDetail.builder()
                .id(1L)
                .status(ApplicationDetailStatus.AWAITING_MENTOR)
                .build();
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.assignMentors(1L, List.of()))
                        .getStatus()
                        .value());
    }

    @Test
    void assignMentorsMovesDetailToCandidateSelection() {
        ApplicationDetail detail = ApplicationDetail.builder()
                .id(1L)
                .status(ApplicationDetailStatus.AWAITING_MENTOR)
                .mentorId(8)
                .build();
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(detailRepository.save(detail)).thenReturn(detail);
        ApplicationDetail result = service.assignMentors(1L, List.of(3, 4));
        assertEquals(List.of(3, 4), result.getAssignedMentorIds());
        assertNull(result.getMentorId());
        assertEquals(ApplicationDetailStatus.AWAITING_CANDIDATE_SELECT_MENTOR, result.getStatus());
    }

    @Test
    void selectMentorRejectsMentorOutsideAssignedList() {
        ApplicationDetail detail = ApplicationDetail.builder()
                .id(1L)
                .status(ApplicationDetailStatus.AWAITING_CANDIDATE_SELECT_MENTOR)
                .assignedMentorIds(List.of(3, 4))
                .build();
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.selectMentor(1L, 9))
                        .getStatus()
                        .value());
    }

    @Test
    void selectMentorPersistsSelectedMentorAndPendingStatus() {
        ApplicationDetail detail = ApplicationDetail.builder()
                .id(1L)
                .roundId(2L)
                .status(ApplicationDetailStatus.AWAITING_CANDIDATE_SELECT_MENTOR)
                .assignedMentorIds(List.of(3, 4))
                .build();
        Round round = Round.builder().id(2L).build();
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(roundRepository.findById(2L)).thenReturn(Optional.of(round));
        when(detailRepository.save(detail)).thenReturn(detail);
        ApplicationDetail result = service.selectMentor(1L, 3);
        assertEquals(3, result.getMentorId());
        assertEquals(ApplicationDetailStatus.PENDING, result.getStatus());
    }

    @Test
    void getAssignedMentorsReturnsEmptyWhenNoneAssigned() {
        ApplicationDetail detail =
                ApplicationDetail.builder().id(1L).assignedMentorIds(List.of()).build();
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        assertEquals(List.of(), service.getAssignedMentors(1L));
    }

    @Test
    void getAssignedMentorsSkipsDeletedMentor() {
        ApplicationDetail detail = ApplicationDetail.builder()
                .id(1L)
                .assignedMentorIds(List.of(3, 4))
                .build();
        MentorResponse mentor = new MentorResponse();
        mentor.setId(3);
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(mentorService.getMentorById(3)).thenReturn(mentor);
        when(mentorService.getMentorById(4)).thenThrow(new RuntimeException("deleted"));
        assertEquals(List.of(mentor), service.getAssignedMentors(1L));
    }

    @Test
    void startAiInterviewRejectsCompletedRound() {
        ApplicationDetail detail = ApplicationDetail.builder()
                .id(1L)
                .status(ApplicationDetailStatus.COMPLETED)
                .build();
        when(detailRepository.findById(1L)).thenReturn(Optional.of(detail));
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.startAiInterview(1L))
                        .getStatus()
                        .value());
    }
}
