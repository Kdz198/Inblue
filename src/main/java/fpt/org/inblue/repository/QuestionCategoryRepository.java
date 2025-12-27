package fpt.org.inblue.repository;

import fpt.org.inblue.model.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, Integer> {
    List<QuestionCategory> findAllByMajor_Id(int majorId);
}
