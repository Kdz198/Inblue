package fpt.org.inblue.controller;

import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SetupJdRoundsRequest;
import fpt.org.inblue.service.RoundService;
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
    public ResponseEntity<List<Round>> setUpRoundForJd(@PathVariable Long jdId, @RequestBody SetupJdRoundsRequest request) {
        List<Round> rounds = roundService.setUpRoundForJd(jdId, request);
        return ResponseEntity.ok(rounds);
    }
}
