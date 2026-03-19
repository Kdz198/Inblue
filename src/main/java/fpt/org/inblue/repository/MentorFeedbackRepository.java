package fpt.org.inblue.repository;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.MentorFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, Integer> {
    List<MentorFeedback> findAllByMentor_Id(int mentorId);

}
