package fpt.org.inblue.repository;

import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.model.enums.QuestionLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Integer> {
    List<PracticeQuestion> findAllByLesson_IdAndLevel(int categoryId, QuestionLevel level);

    @Query("""
        SELECT p FROM PracticeQuestion p
        WHERE p.level = :level
        ORDER BY function('RANDOM')
    """)
    List<PracticeQuestion> findRandomByLevel(
            @Param("level") QuestionLevel level,
            Pageable pageable
    );
}
