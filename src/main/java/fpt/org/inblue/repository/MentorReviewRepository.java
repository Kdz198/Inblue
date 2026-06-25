package fpt.org.inblue.repository;

import fpt.org.inblue.model.MentorReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorReviewRepository extends JpaRepository<MentorReview, Integer> {
    MentorReview findBySession_Id(int sessionId);
}
