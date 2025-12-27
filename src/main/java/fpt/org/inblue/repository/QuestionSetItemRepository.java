package fpt.org.inblue.repository;

import fpt.org.inblue.model.QuestionSetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionSetItemRepository extends JpaRepository<QuestionSetItem, Integer> {
    List<QuestionSetItem> findAllByQuestionSet_QuestionSetId(int questionSetQuestionSetId);
}
