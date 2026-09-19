package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.enums.SessionStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorMapper;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.MentorFeedback;
import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.MentorEventDto;
import fpt.org.inblue.model.dto.request.ChangeMentorPasswordRequest;
import fpt.org.inblue.model.dto.request.CreateMentorRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorRequest;
import fpt.org.inblue.model.dto.response.MentorDashboardSummaryResponse;
import fpt.org.inblue.model.dto.response.MentorDashboardSummaryResponse.FeedbackItem;
import fpt.org.inblue.model.dto.response.MentorDashboardSummaryResponse.ReviewedApplicationItem;
import fpt.org.inblue.model.dto.response.MentorDashboardSummaryResponse.ReviewedCandidateItem;
import fpt.org.inblue.model.dto.response.MentorDashboardSummaryResponse.SessionItem;
import fpt.org.inblue.model.dto.response.MentorDashboardSummaryResponse.UserBasicInfo;
import fpt.org.inblue.model.dto.response.MentorFeedbackResponse;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.model.dto.response.MentorReviewResponse;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.MentorFeedbackRepository;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.MentorReviewRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.EmbeddingService;
import fpt.org.inblue.service.MentorService;
import fpt.org.inblue.utils.FileUtil;
import fpt.org.inblue.utils.VectorUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {
    private static final int TOP_RECOMMENDED_MENTOR_LIMIT = 20;

    private final MentorRepository mentorRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final ApplicationDetailRepository applicationDetailRepository;
    private final ApplicationRepository applicationRepository;
    private final MentorReviewRepository mentorReviewRepository;
    private final MentorFeedbackRepository mentorFeedbackRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CloudinaryService cloudinaryService;
    private final MentorMapper mentorMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmbeddingService embeddingService;

    @Override
    public MentorResponse createMentor(CreateMentorRequest data, MultipartFile avatar) throws IOException {
        if (mentorRepository.existsByEmail(data.getEmail()) || userRepository.existsByEmail(data.getEmail())) {
            throw new CustomException("Email đã tồn tại", HttpStatus.BAD_REQUEST);
        }
        Mentor mentor = mentorMapper.toEntity(data);
        if (data.getPassword() != null && !data.getPassword().isEmpty()) {
            mentor.setPassword(passwordEncoder.encode(data.getPassword()));
        }
        mentor.setRole(Role.MENTOR);
        mentor.setActive(true);
        mentor.setTotalSession(0);
        mentor.setAverageRating(0);
        if (data.getPricePerMinute() != null) {
            mentor.setPricePerMinute(data.getPricePerMinute());
        } else {
            mentor.setPricePerMinute(0);
        }
        float[] skillEmbedding = embeddingService.generateEmbedding(
                data.getProfileData().getSkills().toString());
        mentor.setSkillEmbedding(skillEmbedding);
        mentor = mentorRepository.save(mentor);
        processAndPublishFileEvent(mentor, avatar, "avatar");
        return toMentorResponse(mentor);
    }

    @Override
    public MentorResponse updateMentor(int id, UpdateMentorRequest data, MultipartFile avatar) throws IOException {
        Mentor mentor = mentorRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Mentor Not Found", HttpStatus.NOT_FOUND));

        if (data.getEmail() != null
                && !data.getEmail().equalsIgnoreCase(mentor.getEmail())
                && (mentorRepository.existsByEmailAndIdNot(data.getEmail(), id)
                        || userRepository.existsByEmail(data.getEmail()))) {
            throw new CustomException("Email đã tồn tại", HttpStatus.BAD_REQUEST);
        }

        mentorMapper.updateMentorFromDto(data, mentor);
        float[] skillEmbedding = embeddingService.generateEmbedding(
                data.getProfileData().getSkills().toString());
        mentor.setSkillEmbedding(skillEmbedding);
        mentor = mentorRepository.save(mentor);
        if (avatar != null && !avatar.isEmpty()) {
            if (mentor.getPublic_id() != null) {
                cloudinaryService.deleteImage(mentor.getPublic_id());
            }
            processAndPublishFileEvent(mentor, avatar, "avatar");
        }
        return toMentorResponse(mentor);
    }

    @Override
    public MentorResponse changePassword(int id, ChangeMentorPasswordRequest request) {
        Mentor mentor = mentorRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Mentor Not Found", HttpStatus.NOT_FOUND));

        if (request.getOldPassword() != null && !request.getOldPassword().isEmpty()) {
            if (!passwordEncoder.matches(request.getOldPassword(), mentor.getPassword())) {
                throw new CustomException("Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST);
            }
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new CustomException("Mật khẩu mới không được để trống", HttpStatus.BAD_REQUEST);
        }

        mentor.setPassword(passwordEncoder.encode(request.getNewPassword()));
        mentor = mentorRepository.save(mentor);
        return toMentorResponse(mentor);
    }

    private void processAndPublishFileEvent(Mentor mentor, MultipartFile file, String type) throws IOException {
        if (file != null && !file.isEmpty()) {
            String absolutePath = FileUtil.saveFile(file);
            File tempFile = FileUtil.getFileByPath(absolutePath);
            try {
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(tempFile);
                applicationEventPublisher.publishEvent(new MentorEventDto(mentor, multipartFile, type));
            } finally {
                tempFile.delete();
            }
        }
    }

    @Override
    public MentorResponse getMentorById(int id) {
        if (mentorRepository.existsById(id)) {
            return toMentorResponse(mentorRepository.findById(id).get());
        } else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<MentorResponse> getAllMentors() {
        return toMentorResponseList(mentorRepository.findAll());
    }

    @Override
    public void toggleActive(int id) {
        if (mentorRepository.existsById(id)) {
            Mentor mentor = mentorRepository.findById(id).get();
            if (mentor.isActive()) {
                mentor.setActive(false);
                mentorRepository.save(mentor);
            } else {
                mentor.setActive(true);
                mentorRepository.save(mentor);
            }
        } else {
            throw new CustomException("Mentor not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<MentorResponse> getTopRecommendedMentors(Long jdId) {
        JobDescription jobDescription = jobDescriptionRepository
                .findById(jdId)
                .orElseThrow(() -> new CustomException("Job Description Not Found", HttpStatus.NOT_FOUND));

        float[] jdSkillEmbedding = jobDescription.getSkillEmbedding();
        if (jdSkillEmbedding == null || jdSkillEmbedding.length == 0) {
            return List.of();
        }
        String vectorStr = Arrays.toString(jdSkillEmbedding);
        List<Mentor> mentors = mentorRepository.findTopRecommendedMentor(vectorStr, TOP_RECOMMENDED_MENTOR_LIMIT);

        return mentors.stream()
                .map(mentor -> {
                    MentorResponse response = toMentorResponse(mentor);
                    response.setMatchPercent(
                            VectorUtils.cosineSimilarity(jdSkillEmbedding, mentor.getSkillEmbedding()));
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MentorDashboardSummaryResponse getSummary(int mentorId) {
        List<Session> sessions = sessionRepository.findAllByUserId2(mentorId);
        // MentorReview dùng chung id với session (@MapsId) nên tra theo session của mentor;
        // không dựa vào cột mentor_id vì dữ liệu cũ có thể bị null.
        List<MentorReview> reviews = mentorReviewRepository.findAllById(
                sessions.stream().map(Session::getId).toList());
        List<ReviewedApplicationItem> reviewedApplications = buildReviewedApplications(mentorId, reviews);

        Map<Integer, User> usersById = loadUsers(sessions, reviewedApplications);

        Map<SessionStatus, Long> countByStatus = new EnumMap<>(SessionStatus.class);
        sessions.stream()
                .filter(s -> s.getStatus() != null)
                .forEach(s -> countByStatus.merge(s.getStatus(), 1L, Long::sum));

        List<SessionItem> sessionItems = sessions.stream()
                .sorted(Comparator.comparing(Session::getJoinTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(s -> {
                    User mentee = usersById.get(s.getUserId());
                    return SessionItem.builder()
                            .sessionId(s.getId())
                            .status(s.getStatus())
                            .menteeId(s.getUserId())
                            .menteeName(mentee != null ? mentee.getName() : null)
                            .menteeAvatarUrl(mentee != null ? mentee.getAvatarUrl() : null)
                            .joinTime(s.getJoinTime())
                            .duration(s.getDuration())
                            .totalPrice(s.getTotalPrice())
                            .build();
                })
                .toList();

        reviewedApplications.forEach(item -> {
            User candidate = usersById.get(item.getCandidateId());
            if (candidate != null) {
                item.setCandidateName(candidate.getName());
                item.setCandidateEmail(candidate.getEmail());
                item.setCandidateAvatarUrl(candidate.getAvatarUrl());
            }
        });

        List<FeedbackItem> feedbackItems = mentorFeedbackRepository.findAllByMentor_Id(mentorId).stream()
                .sorted(Comparator.comparingInt(MentorFeedback::getId).reversed())
                .map(fb -> FeedbackItem.builder()
                        .sessionId(fb.getId())
                        .user(toUserBasicInfo(fb.getUser()))
                        .rating(fb.getRating())
                        .comment(fb.getComment())
                        .build())
                .toList();

        Map<Integer, Session> sessionById =
                sessions.stream().collect(Collectors.toMap(Session::getId, Function.identity()));
        List<ReviewedCandidateItem> reviewedCandidateItems = reviews.stream()
                .sorted(Comparator.comparingInt(MentorReview::getId).reversed())
                .map(r -> {
                    User candidate = r.getUser() != null
                            ? r.getUser()
                            : usersById.get(sessionById.get(r.getId()).getUserId());
                    return ReviewedCandidateItem.builder()
                            .sessionId(r.getId())
                            .candidate(toUserBasicInfo(candidate))
                            .review(toReviewResponse(r))
                            .build();
                })
                .toList();

        return MentorDashboardSummaryResponse.builder()
                .totalFeedbacks(feedbackItems.size())
                .feedbacks(feedbackItems)
                .totalReviewedCandidates(reviewedCandidateItems.size())
                .reviewedCandidates(reviewedCandidateItems)
                .totalSessions(sessionItems.size())
                .sessionCountByStatus(countByStatus)
                .sessions(sessionItems)
                .totalReviewedApplications(reviewedApplications.size())
                .reviewedApplications(reviewedApplications)
                .build();
    }

    /** Application detail được gán cho mentor mà mentor đã nộp đánh giá (MentorReview) cho session của vòng đó. */
    private List<ReviewedApplicationItem> buildReviewedApplications(int mentorId, List<MentorReview> reviews) {
        Map<Integer, MentorReview> reviewBySessionId =
                reviews.stream().collect(Collectors.toMap(MentorReview::getId, Function.identity(), (a, b) -> a));
        Map<Integer, MentorFeedback> feedbackBySessionId =
                mentorFeedbackRepository.findAllByMentor_Id(mentorId).stream()
                        .collect(Collectors.toMap(MentorFeedback::getId, Function.identity(), (a, b) -> a));

        List<ApplicationDetail> details = applicationDetailRepository.findAllByMentorId(mentorId).stream()
                .filter(d -> d.getSessionId() != null && reviewBySessionId.containsKey(d.getSessionId()))
                .toList();
        if (details.isEmpty()) {
            return List.of();
        }

        Map<Long, Application> applicationsById = applicationRepository
                .findAllById(details.stream()
                        .map(ApplicationDetail::getApplicationId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Application::getId, Function.identity()));
        Map<Long, String> jobTitleById = jobDescriptionRepository
                .findAllById(applicationsById.values().stream()
                        .map(Application::getJdId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(JobDescription::getId, jd -> String.valueOf(jd.getTitle())));

        return details.stream()
                .filter(d -> {
                    Application app = applicationsById.get(d.getApplicationId());
                    return app != null && !Boolean.TRUE.equals(app.getIsDeleted());
                })
                .map(d -> {
                    Application app = applicationsById.get(d.getApplicationId());
                    MentorReview review = reviewBySessionId.get(d.getSessionId());
                    MentorFeedback feedback = feedbackBySessionId.get(d.getSessionId());
                    return ReviewedApplicationItem.builder()
                            .applicationDetailId(d.getId())
                            .applicationId(d.getApplicationId())
                            .jobTitle(app.getJdId() != null ? jobTitleById.get(app.getJdId()) : null)
                            .sessionId(d.getSessionId())
                            .candidateId(app.getUserId())
                            .mentorReview(toReviewResponse(review))
                            .candidateFeedback(toFeedbackResponse(feedback))
                            .build();
                })
                .toList();
    }

    private Map<Integer, User> loadUsers(List<Session> sessions, List<ReviewedApplicationItem> items) {
        List<Integer> ids = Stream.concat(
                        sessions.stream().map(Session::getUserId),
                        items.stream().map(ReviewedApplicationItem::getCandidateId))
                .distinct()
                .toList();
        return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private UserBasicInfo toUserBasicInfo(User user) {
        if (user == null) {
            return null;
        }
        return UserBasicInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private MentorReviewResponse toReviewResponse(MentorReview review) {
        return MentorReviewResponse.builder()
                .rating(review.getRating())
                .situationNote(review.getSituationNote())
                .taskNote(review.getTaskNote())
                .actionNote(review.getActionNote())
                .resultNote(review.getResultNote())
                .strength(review.getStrength())
                .weakness(review.getWeakness())
                .improve(review.getImprove())
                .build();
    }

    private MentorFeedbackResponse toFeedbackResponse(MentorFeedback feedback) {
        if (feedback == null) {
            return null;
        }
        return MentorFeedbackResponse.builder()
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .userName(feedback.getUser() != null ? feedback.getUser().getName() : null)
                .userAvatarUrl(feedback.getUser() != null ? feedback.getUser().getAvatarUrl() : null)
                .build();
    }

    private MentorResponse toMentorResponse(Mentor mentor) {
        MentorResponse response = mentorMapper.toMentorResponse(mentor);
        List<MentorFeedback> feedbacks = mentorFeedbackRepository.findAllByMentor_Id(mentor.getId());
        List<MentorFeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(fb -> MentorFeedbackResponse.builder()
                        .rating(fb.getRating())
                        .comment(fb.getComment())
                        .userName(fb.getUser() != null ? fb.getUser().getName() : null)
                        .userAvatarUrl(fb.getUser() != null ? fb.getUser().getAvatarUrl() : null)
                        .build())
                .toList();
        response.setFeedbacks(feedbackResponses);
        return response;
    }

    private List<MentorResponse> toMentorResponseList(List<Mentor> mentors) {
        return mentors.stream().map(this::toMentorResponse).toList();
    }
}
