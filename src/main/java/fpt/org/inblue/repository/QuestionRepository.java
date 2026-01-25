package fpt.org.inblue.repository;

import fpt.org.inblue.model.Question;
import fpt.org.inblue.model.enums.QuestionLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findAllByLesson_IdAndLevel(int categoryId, QuestionLevel level);

    @Query("""
        SELECT q FROM Question q
        WHERE q.level = :level
        ORDER BY function('RANDOM')
    """)
    List<Question> findRandomByLevel(
            @Param("level") QuestionLevel level,
            Pageable pageable
    );
}
