package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.MemberShipPlan;
import fpt.org.inblue.model.enums.PlanName;
import fpt.org.inblue.repository.MemberShipPlanRepository;
import fpt.org.inblue.service.MemberShipPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberShipPlanServiceImpl implements MemberShipPlanService {

    @Autowired
    private MemberShipPlanRepository memberShipPlanRepository;

    @Override
    public List<MemberShipPlan> getAllPlans() {
        return memberShipPlanRepository.findAll();
    }

    @Override
    public MemberShipPlan getPlanById(int id) {
        return memberShipPlanRepository.findById(id)
                .orElseThrow(() -> new CustomException("Gói không tồn tại", HttpStatus.NOT_FOUND));
    }

    @Override
    public MemberShipPlan createPlan(MemberShipPlan plan) {
       if(plan.getName().equals(PlanName.NEW) || plan.getName().equals(PlanName.FREE) || plan.getName().equals(PlanName.TEST)){
           plan.setDurationDays(Integer.MAX_VALUE);
       }
        return memberShipPlanRepository.save(plan);
    }

    @Override
    public MemberShipPlan updatePlan(MemberShipPlan plan) {
        if (!memberShipPlanRepository.existsById(plan.getId())) {
            throw new CustomException("Gói không tồn tại", HttpStatus.NOT_FOUND);
        }
        return memberShipPlanRepository.save(plan);
    }

    @Override
    public void deletePlan(int id) {
        if (!memberShipPlanRepository.existsById(id)) {
            throw new CustomException("Gói không tồn tại", HttpStatus.NOT_FOUND);
        }
        memberShipPlanRepository.deleteById(id);
    }
}

