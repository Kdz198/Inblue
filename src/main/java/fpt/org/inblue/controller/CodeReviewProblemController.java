package fpt.org.inblue.controller;

import fpt.org.inblue.model.CodeReviewProblem;
import fpt.org.inblue.model.dto.request.CodeReviewProblemGenerateRequest;
import fpt.org.inblue.model.dto.response.CodeReviewProblemGenerateResponse;
import fpt.org.inblue.service.CodeReviewProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/code-review-problems")
@RequiredArgsConstructor
public class CodeReviewProblemController {

    private final CodeReviewProblemService codeReviewProblemService;

    @GetMapping
    public ResponseEntity<List<CodeReviewProblem>> getAllCodeReviewProblems() {
        List<CodeReviewProblem> problems = codeReviewProblemService.findAllCodeReviewProblems();
        return ResponseEntity.ok(problems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeReviewProblem> getCodeReviewProblemById(@PathVariable Long id) {
        Optional<CodeReviewProblem> problem = codeReviewProblemService.findCodeReviewProblemById(id);
        return problem.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CodeReviewProblem> createCodeReviewProblem(@RequestBody CodeReviewProblem problem) {
        CodeReviewProblem savedProblem = codeReviewProblemService.save(problem);
        return ResponseEntity.ok(savedProblem);
    }

    @PostMapping("/generate")
    public ResponseEntity<CodeReviewProblemGenerateResponse> generateCodeReviewProblem(@RequestBody CodeReviewProblemGenerateRequest request) {
        CodeReviewProblemGenerateResponse response = codeReviewProblemService.generateCodeReviewProblem(request);
        return ResponseEntity.ok(response);
    }
}
