package fpt.org.inblue.repository;

import fpt.org.inblue.model.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionBankRepository extends JpaRepository<QuestionBank,Integer> {
    QuestionBank getQuestionBankById(int id);
}

