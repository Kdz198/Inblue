package fpt.org.inblue.repository;

import fpt.org.inblue.model.Question;
import fpt.org.inblue.model.enums.QuestionLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findAllByCategory_IdAndLevel(int categoryId, QuestionLevel level);
}
