package fpt.org.inblue.controller;

import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SetupJdRoundsRequest;
import fpt.org.inblue.model.dto.request.UpdateJdRoundRequest;
import fpt.org.inblue.service.RoundService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rounds")
public class RoundController {
    @Autowired
    private RoundService roundService;

    @PutMapping("/jd/{jdId}")
    @Operation(summary = "Thiết lập các vòng phỏng vấn cho một Job Description",
            description = "Cho phép nhà tuyển dụng thiết lập các vòng phỏng vấn cho một JD cụ thể. ")
    public ResponseEntity<List<Round>> setUpRoundForJd(@PathVariable Long jdId, @RequestBody SetupJdRoundsRequest request) {
        List<Round> rounds = roundService.setUpRoundForJd(jdId, request);
        return ResponseEntity.ok(rounds);
    }

    @PutMapping("/jd/{jdId}/update")
    @Operation(summary = "Cập nhật các vòng phỏng vấn cho một Job Description",
            description = "Cho phép nhà tuyển dụng cập nhật các vòng phỏng vấn đã thiết lập cho một JD cụ thể. ")
    public ResponseEntity<List<Round>> updateRoundForJd(@PathVariable Long jdId, @RequestBody UpdateJdRoundRequest request) {
        List<Round> rounds = roundService.updateRoundForJd(jdId, request);
        return ResponseEntity.ok(rounds);   }
}
