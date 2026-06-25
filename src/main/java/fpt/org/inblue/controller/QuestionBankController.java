package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.model.dto.request.CreateQuestionBankRequest;
import fpt.org.inblue.model.dto.request.UpdateQuestionBankRequest;
import fpt.org.inblue.model.dto.response.QuestionGenerateResponse;
import fpt.org.inblue.service.QuestionBankService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/question-banks")
@RequiredArgsConstructor
public class QuestionBankController {
    private final QuestionBankService questionBankService;

    @GetMapping
    public ResponseEntity<List<QuestionBank>> getQuestionBank() {
        return ResponseEntity.ok(questionBankService.getAllQuestionBanks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionBank> getQuestionBankById(@PathVariable Integer id) {
        return ResponseEntity.ok(questionBankService.getQuestionBankById(id));
    }

    @PostMapping
    public ResponseEntity<QuestionBank> createQuestionBank(@Valid @RequestBody CreateQuestionBankRequest request) {
        return ResponseEntity.ok(questionBankService.createQuestionBank(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<QuestionBank> updateQuestionBank(
            @PathVariable Integer id, @Valid @RequestBody UpdateQuestionBankRequest request) {
        return ResponseEntity.ok(questionBankService.updateQuestionBank(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionBank(@PathVariable Integer id) {
        questionBankService.deleteQuestionBank(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<QuestionGenerateResponse> generateQuestion(
            @RequestBody fpt.org.inblue.model.dto.request.QuestionGenerateRequest request) {
        QuestionGenerateResponse response = questionBankService.generateQuestion(request);
        return ResponseEntity.ok(response);
    }
}
