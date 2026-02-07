package fpt.org.inblue.repository;

import fpt.org.inblue.model.QuizItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizItemRepository extends JpaRepository<QuizItem, Integer> {
    List<QuizItem> findAllByQuizSet_QuizId(int quizSetQuizId);
}
