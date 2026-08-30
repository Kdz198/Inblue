package fpt.org.inblue.repository;

import fpt.org.inblue.model.QuestionBank;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

public interface QuestionBankRepository extends JpaRepository<QuestionBank, Integer> {
    QuestionBank getQuestionBankById(int id);

    @Query(
            "select q from QuestionBank q "
                    + "where q.isDeleted = false "
                    + "and upper(q.questionCategory.name) = upper(:categoryName) "
                    + "order by function('random')")
    List<QuestionBank> findRandomByCategoryName(String categoryName, Pageable pageable);

    @Query(
            "select q from QuestionBank q "
                    + "where q.isDeleted = false "
                    + "and upper(q.questionCategory.name) in :categoryNames "
                    + "order by function('random')")
    List<QuestionBank> findRandomByCategoryNames(Collection<String> categoryNames, Pageable pageable);
}
