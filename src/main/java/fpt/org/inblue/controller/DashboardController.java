package fpt.org.inblue.controller;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    @Autowired
    DashboardService dashboardService;

    @GetMapping("/total-mentor")
    public ResponseEntity<Integer> getTotalMentor() {
        return ResponseEntity.ok(dashboardService.getMentorTotal());
    }
    @GetMapping("/total-user")
    public ResponseEntity<Integer> getTotalUser() {
        return ResponseEntity.ok(dashboardService.getUserTotal());
    }

     @GetMapping("total-income")
        public ResponseEntity<List<Payment>> getTotalIncome() {
            return ResponseEntity.ok(dashboardService.getPayments());
        }
        @GetMapping("/total-session")
        public ResponseEntity<Integer> getTotalSession() {
            return ResponseEntity.ok(dashboardService.getSessionTotal());
        }
}
