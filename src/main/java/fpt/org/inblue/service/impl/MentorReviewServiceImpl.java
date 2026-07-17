package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.enums.SessionStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorReviewMapper;
import fpt.org.inblue.model.*;
import fpt.org.inblue.model.dto.request.CreateMentorReviewRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorReviewRequest;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.MentorReviewService;
import fpt.org.inblue.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentorReviewServiceImpl implements MentorReviewService {
    private final MentorReviewRepository repo;
    private final SessionRepository sessionRepo;
    private final MentorReviewMapper mentorReviewMapper;
    private final MentorRepository mentorRepo;
    private final UserService userService;
    private final MentorInterviewBookingRepository bookingRepo;
    private final ApplicationDetailRepository appDetailRepo;
    private final RoundRepository roundRepo;
    private final ApplicationService applicationService;
    private final MentorFeedbackRepository mentorFeedbackRepository;

    @Override
    @Transactional
    public MentorReview mentorReview(CreateMentorReviewRequest mentorReview) {
        Mentor mentor = mentorRepo.getMentorById(mentorReview.getMentorId());
        User user = userService.getById(mentorReview.getUserId());
        Session session = sessionRepo.findById(mentorReview.getSessionId()).orElse(null);
        if (session == null || user == null || mentor == null) {
            throw new CustomException("Session| Mentor| User not found", HttpStatus.NOT_FOUND);
        }

        // Check if there is an associated Kiosk Booking
        boolean isKioskBooking = false;
        java.util.Optional<MentorInterviewBooking> bookingOpt = bookingRepo.findBySessionId(session.getId());
        if (bookingOpt.isPresent()) {
            isKioskBooking = true;
            // Force complete the session and booking
            session.setStatus(SessionStatus.COMPLETED);
            sessionRepo.save(session);

            MentorInterviewBooking booking = bookingOpt.get();
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepo.save(booking);
        }

        if (session.getStatus().equals(SessionStatus.COMPLETED)) {
            MentorReview review = mentorReviewMapper.toEntity(mentorReview);
            review.setSession(session);
            review = repo.save(review);

            if (isKioskBooking) {
                MentorInterviewBooking booking = bookingOpt.get();
                ApplicationDetail appDetail =
                        appDetailRepo.findById(booking.getApplicationDetailId()).orElse(null);
                if (appDetail != null) {
                    appDetail.setMentorReview(review);
                    appDetail.setStatus(ApplicationDetailStatus.COMPLETED);
                    appDetail.setCompletedAt(java.time.LocalDateTime.now());

                    // Fetch Round config to calculate scores
                    Round round = roundRepo.findById(appDetail.getRoundId()).orElse(null);
                    double maxScore = 100.0;
                    if (round != null
                            && round.getConfigData() != null
                            && round.getConfigData().getMaxScore() != null) {
                        maxScore = round.getConfigData().getMaxScore();
                    }

                    double score = (review.getRating() / 10.0) * maxScore;
                    appDetail.setFinalScore(score);

                    if (round != null && round.getPassThreshold() != null) {
                        appDetail.setFinalResult(
                                score >= round.getPassThreshold()
                                        ? ApplicationDetail.RoundResult.PASSED
                                        : ApplicationDetail.RoundResult.FAILED);
                    } else {
                        appDetail.setFinalResult(ApplicationDetail.RoundResult.PASSED);
                    }
                    appDetailRepo.save(appDetail);
                    // move to next round
                    Application application = applicationService.getApplicationById(appDetail.getApplicationId());
                    applicationService.moveToNextRound(application);
                }
            }

            checkAndCompleteRound(session.getId());

            return review;
        } else {
            throw new CustomException(
                    "Cannot review mentor for a session that is not completed", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public MentorReview updateMentorReview(UpdateMentorReviewRequest mentorReview) {
        if (repo.existsById(mentorReview.getId())) {
            MentorReview review = repo.findById(mentorReview.getId()).orElse(null);
            mentorReviewMapper.fromUpdateToEntity(mentorReview, review);
            return repo.save(review);
        } else {
            throw new CustomException("Mentor review not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public MentorReview getMentorReviewById(int id) {
        if (sessionRepo.existsById(id)) {
            return repo.findBySession_Id(id);
        } else {
            throw new CustomException("Mentor review not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<MentorReview> getAllMentorReviews() {
        return repo.findAll();
    }

    @Override
    @Transactional
    public void checkAndCompleteRound(int sessionId) {
        MentorReview review = repo.findBySession_Id(sessionId);
        MentorFeedback feedback =
                mentorFeedbackRepository.findBySession_Id(sessionId).orElse(null);

        // Chỉ khi cả 2 review và feedback đều tồn tại
        if (review != null && feedback != null) {
            ApplicationDetail appDetail =
                    appDetailRepo.findBySessionId((long) sessionId).orElse(null);
            if (appDetail != null && appDetail.getStatus() != ApplicationDetailStatus.COMPLETED) {
                appDetail.setMentorReview(review);
                appDetail.setStatus(ApplicationDetailStatus.COMPLETED);
                appDetail.setCompletedAt(java.time.LocalDateTime.now());

                // Fetch Round config to calculate scores
                Round round = roundRepo.findById(appDetail.getRoundId()).orElse(null);
                double maxScore = 100.0;
                if (round != null
                        && round.getConfigData() != null
                        && round.getConfigData().getMaxScore() != null) {
                    maxScore = round.getConfigData().getMaxScore();
                }

                // Điểm số tính theo rating của MentorReview
                double score = (review.getRating() / 10.0) * maxScore;
                appDetail.setFinalScore(score);

                if (round != null && round.getPassThreshold() != null) {
                    appDetail.setFinalResult(
                            score >= round.getPassThreshold()
                                    ? ApplicationDetail.RoundResult.PASSED
                                    : ApplicationDetail.RoundResult.FAILED);
                } else {
                    appDetail.setFinalResult(ApplicationDetail.RoundResult.PASSED);
                }

                appDetailRepo.save(appDetail);

                // Di chuyển sang vòng tiếp theo
                Application application = applicationService.getApplicationById(appDetail.getApplicationId());
                applicationService.moveToNextRound(application);
            }
        }
    }
}
