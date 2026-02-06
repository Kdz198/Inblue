package fpt.org.inblue.repository;

import fpt.org.inblue.model.PracticeSetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeSetItemRepository extends JpaRepository<PracticeSetItem, Integer> {
    List<PracticeSetItem> findAllByPracticeSet_Id(int id);
}
