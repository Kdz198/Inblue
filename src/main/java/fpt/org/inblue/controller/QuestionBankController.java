package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/question-banks")
public class QuestionBankController {
    @Autowired
    private QuestionBankService questionBankService;

    @GetMapping
    public ResponseEntity<List<QuestionBank>> getQuestionBank() {
        return ResponseEntity.ok(questionBankService.getAllQuestionBanks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionBank> getQuestionBankById(@PathVariable Integer id) {
        return ResponseEntity.ok(questionBankService.getQuestionBankById(id));
    }

    @PostMapping
    public ResponseEntity<QuestionBank> createQuestionBank(@RequestBody QuestionBank questionBank) {
        return ResponseEntity.ok(questionBankService.createQuestionBank(questionBank));
    }

    @PutMapping("{id}")
    public ResponseEntity<QuestionBank> updateQuestionBank(@PathVariable Integer id, @RequestBody QuestionBank questionBank) {
        return ResponseEntity.ok(questionBankService.updateQuestionBank(id, questionBank));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionBank(@PathVariable Integer id) {
        questionBankService.deleteQuestionBank(id);
        return ResponseEntity.noContent().build();}
}
