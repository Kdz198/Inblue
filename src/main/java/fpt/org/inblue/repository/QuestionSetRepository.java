package fpt.org.inblue.repository;

import fpt.org.inblue.model.QuestionSet;
import fpt.org.inblue.model.enums.TargetLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionSetRepository extends JpaRepository<QuestionSet, Integer> {
    List<QuestionSet> findAllByLevel(TargetLevel level);
}
