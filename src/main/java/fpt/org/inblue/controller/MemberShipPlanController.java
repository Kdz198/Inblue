package fpt.org.inblue.controller;

import fpt.org.inblue.model.MemberShipPlan;
import fpt.org.inblue.service.MemberShipPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership-plans")
@CrossOrigin(origins = "*")
public class MemberShipPlanController {

    @Autowired
    private MemberShipPlanService memberShipPlanService;

    @GetMapping
    public ResponseEntity<List<MemberShipPlan>> getAllPlans() {
        return ResponseEntity.ok(memberShipPlanService.getAllPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberShipPlan> getPlanById(@PathVariable int id) {
        return ResponseEntity.ok(memberShipPlanService.getPlanById(id));
    }

    @PostMapping
    public ResponseEntity<MemberShipPlan> createPlan(@RequestBody MemberShipPlan plan) {
        return ResponseEntity.ok(memberShipPlanService.createPlan(plan));
    }

    @PutMapping
    public ResponseEntity<MemberShipPlan> updatePlan(@RequestBody MemberShipPlan plan) {
        return ResponseEntity.ok(memberShipPlanService.updatePlan(plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable int id) {
        memberShipPlanService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}

