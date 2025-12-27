package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.enums.SessionStatus;
import fpt.org.inblue.repository.MentorReviewRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.service.MentorReviewService;
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

    @Override
    public MentorReview mentorReview(MentorReview mentorReview) {
        Session session = sessionRepo.findById(mentorReview.getSession().getId()).orElse(null);
        if(session.getStatus().equals(SessionStatus.COMPLETED)) {
            return repo.save(mentorReview);
        }
        else{
            throw new CustomException("Cannot review mentor for a session that is not completed", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public MentorReview updateMentorReview(MentorReview mentorReview) {
        if(repo.existsById(mentorReview.getId())) {
            return repo.save(mentorReview);
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
