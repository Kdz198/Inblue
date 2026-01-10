package fpt.org.inblue.service;

import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.dto.request.CreateMentorReviewRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorReviewRequest;

import java.util.List;

public interface MentorReviewService {
    MentorReview mentorReview(CreateMentorReviewRequest mentorReview);
    MentorReview updateMentorReview(UpdateMentorReviewRequest mentorReview);
    MentorReview getMentorReviewById(int id);
    List<MentorReview> getAllMentorReviews();
}
