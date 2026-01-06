package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuestionSet;
import fpt.org.inblue.model.QuestionSetItem;
import fpt.org.inblue.service.QuestionSetItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-set-items")
public class QuestionSetItemController {
    @Autowired
    private QuestionSetItemService questionSetItemService;

    @GetMapping
    public ResponseEntity<List<QuestionSetItem>> getQuestionSetItems() {
        List<QuestionSetItem> questionSetItems = questionSetItemService.getAllQuestionSetItems();
        return ResponseEntity.ok(questionSetItems);
    }
    @GetMapping("{id}")
    public ResponseEntity<QuestionSetItem> getQuestionSetItem(@PathVariable int id) {
        QuestionSetItem questionSetItem = questionSetItemService.getQuestionSetItem(id);
        return ResponseEntity.ok(questionSetItem);}

    @GetMapping("/by-question-set/{id}")
    public ResponseEntity<List<QuestionSetItem>> getQuestionSetItemsByQuestionSet(@PathVariable int id) {
        List<QuestionSetItem> questionSetItems = questionSetItemService.getQuestionSetItemsByQuestionSet(id);
        return ResponseEntity.ok(questionSetItems);}

    @PostMapping
    public ResponseEntity<QuestionSetItem> createQuestionSetItem(@RequestBody QuestionSetItem questionSetItem) {
        QuestionSetItem createdQuestionSetItem = questionSetItemService.createQuestionSetItem(questionSetItem);
        return ResponseEntity.ok(createdQuestionSetItem);}

    @PutMapping
    public ResponseEntity<QuestionSetItem> updateQuestionSetItem(@RequestBody QuestionSetItem questionSetItem) {
        QuestionSetItem updatedQuestionSetItem = questionSetItemService.updateQuestionSetItem(questionSetItem);
        return ResponseEntity.ok(updatedQuestionSetItem);}

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQuestionSetItem(@PathVariable int id) {
        questionSetItemService.deleteQuestionSetItem(id);
        return ResponseEntity.noContent().build();}

    @PostMapping("create-items")
    public ResponseEntity<List<QuestionSetItem>> createQuestionSetItems(@RequestParam int easy, @RequestParam int medium, @RequestParam int hard, @RequestBody QuestionSet questionSet) {
        List<QuestionSetItem> questionSetItems = questionSetItemService.createQuestionSetItems(easy, medium, hard, questionSet);
    return ResponseEntity.ok(questionSetItems);}
}
