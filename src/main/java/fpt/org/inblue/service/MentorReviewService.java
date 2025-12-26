package fpt.org.inblue.service;

import fpt.org.inblue.model.MentorReview;

import java.util.List;

public interface MentorReviewService {
    MentorReview mentorReview(MentorReview mentorReview);
    MentorReview updateMentorReview(MentorReview mentorReview);
    MentorReview getMentorReviewById(int id);
    List<MentorReview> getAllMentorReviews();
}
