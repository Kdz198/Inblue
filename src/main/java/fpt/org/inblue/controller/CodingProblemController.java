package fpt.org.inblue.controller;

import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.model.dto.request.CodingProblemGenerateRequest;
import fpt.org.inblue.model.dto.response.CodingProblemGenerateResponse;
import fpt.org.inblue.service.CodingProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/coding-problems")
public class CodingProblemController {
    @Autowired
    private CodingProblemService codingProblemService;

    @GetMapping
    public ResponseEntity<List<CodingProblem>> getAllCodingProblems() {
        List<CodingProblem> codingProblems = codingProblemService.findAllCodingProblems();
        return ResponseEntity.ok(codingProblems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<CodingProblem>> getCodingProblemById(@PathVariable Long id) {
        return ResponseEntity.ok(codingProblemService.findCodingProblemById(id));
    }
    @PostMapping
    public ResponseEntity<CodingProblem> createCodingProblem(@RequestBody CodingProblem codingProblem) {
        CodingProblem savedCodingProblem = codingProblemService.save(codingProblem);
        return ResponseEntity.ok(savedCodingProblem);
    }

    @PostMapping("/generate")
    public ResponseEntity<CodingProblemGenerateResponse> generateCodingProblem(@RequestBody CodingProblemGenerateRequest request) {
        CodingProblemGenerateResponse response = codingProblemService.generateCodingProblem(request);
        return ResponseEntity.ok(response);
    }

}
