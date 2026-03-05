package fpt.org.inblue.repository;

import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.enums.TargetLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeSetRepository extends JpaRepository<PracticeSet, Integer> {
    List<PracticeSet> findAllByLevel(TargetLevel level);

    PracticeSet findById(int id);

    List<PracticeSet> findAllByInterviewSessionId(Integer interviewSessionId);

    List<PracticeSet> findAllByUser_Id(int userId);
}
