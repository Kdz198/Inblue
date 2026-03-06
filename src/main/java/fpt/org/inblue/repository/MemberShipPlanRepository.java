package fpt.org.inblue.repository;

import fpt.org.inblue.model.MemberShipPlan;
import fpt.org.inblue.model.enums.PlanName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberShipPlanRepository extends JpaRepository<MemberShipPlan, Integer> {
    MemberShipPlan findByName(PlanName name);
}

