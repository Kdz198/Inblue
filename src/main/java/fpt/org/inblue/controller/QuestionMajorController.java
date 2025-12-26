package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuestionMajor;
import fpt.org.inblue.service.QuestionMajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-majors")
public class QuestionMajorController {
    @Autowired
    private QuestionMajorService questionMajorService;

    @GetMapping
    public ResponseEntity<List<QuestionMajor>> getAllQuestionMajors() {
        List<QuestionMajor> questionMajors = questionMajorService.getAllQuestionMajors();
        return ResponseEntity.ok(questionMajors);
    }
    @PostMapping
    public ResponseEntity<QuestionMajor> createQuestionMajor(QuestionMajor questionMajor) {
        QuestionMajor createdQuestionMajor = questionMajorService.createQuestionMajor(questionMajor);
        return ResponseEntity.ok(createdQuestionMajor);
    }
    @PutMapping
    public ResponseEntity<QuestionMajor> updateQuestionMajor(QuestionMajor questionMajor) {
        QuestionMajor updatedQuestionMajor = questionMajorService.updateQuestionMajor(questionMajor);
        return ResponseEntity.ok(updatedQuestionMajor);
    }
    @GetMapping("/{id}")
    public ResponseEntity<QuestionMajor> getQuestionMajorById(@PathVariable int id) {
        QuestionMajor questionMajor = questionMajorService.getQuestionMajorById(id);
        return ResponseEntity.ok(questionMajor);}
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteQuestionMajor(@PathVariable int id) {
        return  ResponseEntity.ok(questionMajorService.deleteQuestionMajor(id));
        }
}

