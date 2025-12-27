package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuestionSet;
import fpt.org.inblue.service.QuestionSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-sets")
public class QuestionSetController {
    @Autowired
    private QuestionSetService questionSetService;

    @GetMapping
    public ResponseEntity<List<QuestionSet>> getQuestionSets() {
        List<QuestionSet> questionSets = questionSetService.getAllQuestionSets();
        return ResponseEntity.ok(questionSets);
    }
    @GetMapping("{id}")
    public ResponseEntity<QuestionSet> getQuestionSet(@PathVariable int id) {
        QuestionSet questionSet = questionSetService.getQuestionSet(id);
        return ResponseEntity.ok(questionSet);
    }
    @GetMapping("/level/{level} ")
    public ResponseEntity<List<QuestionSet>> getQuestionSetsByTargetLevel(@PathVariable String level) {
        List<QuestionSet> questionSets = questionSetService.getQuestionSetsByTargetLevel(level);
        return ResponseEntity.ok(questionSets);}
    @PostMapping
    public ResponseEntity<QuestionSet> createQuestionSet(@RequestBody QuestionSet questionSet) {
        QuestionSet createdQuestionSet = questionSetService.createQuestionSet(questionSet);
        return ResponseEntity.ok(createdQuestionSet);
    }
    @PutMapping
    public ResponseEntity<QuestionSet> updateQuestionSet(@RequestBody QuestionSet questionSet) {
        QuestionSet updatedQuestionSet = questionSetService.updateQuestionSet(questionSet);
        return ResponseEntity.ok(updatedQuestionSet);}

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQuestionSet(@PathVariable int id) {
        questionSetService.deleteQuestionSet(id);
        return ResponseEntity.noContent().build();}
}
