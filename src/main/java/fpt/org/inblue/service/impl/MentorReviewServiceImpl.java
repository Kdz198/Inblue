package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorReviewMapper;
import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.request.CreateMentorReviewRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorReviewRequest;
import fpt.org.inblue.model.enums.SessionStatus;
import fpt.org.inblue.repository.MentorReviewRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.service.MentorReviewService;
import fpt.org.inblue.service.MentorService;
import fpt.org.inblue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MentorReviewServiceImpl implements MentorReviewService {
    @Autowired
    private MentorReviewRepository repo;

    @Autowired
    private SessionRepository sessionRepo;
    @Autowired
    private MentorReviewMapper mentorReviewMapper;
    @Autowired
    private MentorService mentorService;
    @Autowired
    private UserService userService;

    @Override
    public MentorReview mentorReview(CreateMentorReviewRequest mentorReview) {
        Mentor mentor = mentorService.getMentorById(mentorReview.getMentorId());
        User user = userService.getById(mentorReview.getUserId());
        Session session = sessionRepo.findById(mentorReview.getSessionId()).orElse(null);
        if(session==null || user==null || mentor==null){
            throw new CustomException("Session| Mentor| User not found", HttpStatus.NOT_FOUND);
        }
        if(session.getStatus().equals(SessionStatus.COMPLETED)) {
            MentorReview review = mentorReviewMapper.toEntity(mentorReview);
            review.setSession(session);
            return repo.save(review);
        }
        else{
            throw new CustomException("Cannot review mentor for a session that is not completed", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public MentorReview updateMentorReview(UpdateMentorReviewRequest mentorReview) {
        if(repo.existsById(mentorReview.getId())) {
            MentorReview review = repo.findById(mentorReview.getId()).orElse(null);
            mentorReviewMapper.fromUpdateToEntity(mentorReview, review);
            return repo.save(review);
        }
        else {
            throw new CustomException("Mentor review not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public MentorReview getMentorReviewById(int id) {
        if(sessionRepo.existsById(id)) {
            return repo.findBySession_Id(id);
        }
        else {
            throw new CustomException("Mentor review not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<MentorReview> getAllMentorReviews() {
        return repo.findAll();
    }
}
