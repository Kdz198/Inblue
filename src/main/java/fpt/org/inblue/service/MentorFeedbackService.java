package fpt.org.inblue.service;

import fpt.org.inblue.model.MentorFeedback;
import fpt.org.inblue.model.dto.request.CreateMentorFeedbackRequest;
import fpt.org.inblue.model.dto.request.CreateMentorReviewRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorFeedbackRequest;

import java.util.List;

public interface MentorFeedbackService {
    MentorFeedback createMentorFeedback(CreateMentorFeedbackRequest mentorFeedback);
    MentorFeedback updateMentorFeedback(UpdateMentorFeedbackRequest mentorFeedback);
    MentorFeedback getMentorFeedbackBySessionId(int sessionId);
    List<MentorFeedback> getAllByMentor(int mentorId);
    List<MentorFeedback> getAllMentorFeedbacks();
}
