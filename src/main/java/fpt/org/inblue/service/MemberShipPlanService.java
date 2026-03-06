package fpt.org.inblue.service;

import fpt.org.inblue.model.MemberShipPlan;

import java.util.List;

public interface MemberShipPlanService {
    List<MemberShipPlan> getAllPlans();

    MemberShipPlan getPlanById(int id);

    MemberShipPlan createPlan(MemberShipPlan plan);

    MemberShipPlan updatePlan(MemberShipPlan plan);

    void deletePlan(int id);
}

