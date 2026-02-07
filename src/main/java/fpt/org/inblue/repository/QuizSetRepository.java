package fpt.org.inblue.repository;

import fpt.org.inblue.model.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSetRepository extends JpaRepository<QuizSet, Integer> {
    List<QuizSet> findAllByPracticeSet_Id(int practiceSetId);
}
