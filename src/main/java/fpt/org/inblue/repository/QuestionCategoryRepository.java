package fpt.org.inblue.repository;

import fpt.org.inblue.model.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory,Integer> {
}
