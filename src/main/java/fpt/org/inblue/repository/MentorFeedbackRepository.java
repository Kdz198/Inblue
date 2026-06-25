package fpt.org.inblue.repository;

import fpt.org.inblue.model.MentorFeedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, Integer> {
    List<MentorFeedback> findAllByMentor_Id(int mentorId);
}
