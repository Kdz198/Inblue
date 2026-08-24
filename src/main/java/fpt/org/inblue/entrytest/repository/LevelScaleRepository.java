package fpt.org.inblue.entrytest.repository;

import fpt.org.inblue.entrytest.entity.LevelScale;
import fpt.org.inblue.entrytest.enums.TargetRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LevelScaleRepository extends JpaRepository<LevelScale, Long> {
    List<LevelScale> findAllByIsActiveTrue();

    List<LevelScale> findAllByTargetRole(TargetRole targetRole);

    List<LevelScale> findAllByTargetRoleIsNull();
}
