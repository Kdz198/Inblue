package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.MentorFeedbackMapper;
import fpt.org.inblue.model.MentorFeedback;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.dto.request.CreateMentorFeedbackRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorFeedbackRequest;
import fpt.org.inblue.repository.MentorFeedbackRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.service.MentorFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MentorFeedbackServiceImpl implements MentorFeedbackService {

    @Autowired
    private MentorFeedbackRepository mentorFeedbackRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private MentorFeedbackMapper mentorFeedbackMapper;

    @Override
    public MentorFeedback createMentorFeedback(CreateMentorFeedbackRequest mentorFeedback) {
        Session session = sessionRepository.findById(mentorFeedback.getSessionId()).get();
        if(session!=null){
            MentorFeedback feedback = mentorFeedbackMapper.toEntity(mentorFeedback);
            feedback.setSession(session);
            return mentorFeedbackRepository.save(feedback);
        }
        else{
            throw new CustomException("Session not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public MentorFeedback updateMentorFeedback(UpdateMentorFeedbackRequest mentorFeedback) {
        if(mentorFeedbackRepository.existsById(mentorFeedback.getId())) {
            MentorFeedback feedback = mentorFeedbackRepository.findById(mentorFeedback.getId()).get();
            mentorFeedbackMapper.fromUpdateToEntity(mentorFeedback, feedback);
            return mentorFeedbackRepository.save(feedback);
        }
        else {
            throw new CustomException("Mentor feedback not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public MentorFeedback getMentorFeedbackBySessionId(int sessionId) {
        return mentorFeedbackRepository.findById(sessionId).get();
    }

    @Override
    public List<MentorFeedback> getAllByMentor(int mentorId) {
        return mentorFeedbackRepository.findAllByMentor_Id(mentorId);
    }

    @Override
    public List<MentorFeedback> getAllMentorFeedbacks() {
        return mentorFeedbackRepository.findAll();
    }
}
