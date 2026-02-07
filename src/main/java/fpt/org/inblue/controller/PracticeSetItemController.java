package fpt.org.inblue.controller;

import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.PracticeSetItem;
import fpt.org.inblue.service.PracticeSetItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practice-set-items")
@CrossOrigin(origins = "*")
public class PracticeSetItemController {
    @Autowired
    private PracticeSetItemService practiceSetItemService;

    @GetMapping
    public ResponseEntity<List<PracticeSetItem>> getQuestionSetItems() {
        List<PracticeSetItem> practiceSetItems = practiceSetItemService.getAllQuestionSetItems();
        return ResponseEntity.ok(practiceSetItems);
    }
    @GetMapping("{id}")
    public ResponseEntity<PracticeSetItem> getQuestionSetItem(@PathVariable int id) {
        PracticeSetItem practiceSetItem = practiceSetItemService.getQuestionSetItem(id);
        return ResponseEntity.ok(practiceSetItem);}

    @GetMapping("/by-question-set/{id}")
    public ResponseEntity<List<PracticeSetItem>> getQuestionSetItemsByQuestionSet(@PathVariable int id) {
        List<PracticeSetItem> practiceSetItems = practiceSetItemService.getQuestionSetItemsByQuestionSet(id);
        return ResponseEntity.ok(practiceSetItems);}

    @PostMapping
    public ResponseEntity<PracticeSetItem> createQuestionSetItem(@RequestBody PracticeSetItem practiceSetItem) {
        PracticeSetItem createdPracticeSetItem = practiceSetItemService.createQuestionSetItem(practiceSetItem);
        return ResponseEntity.ok(createdPracticeSetItem);}

    @PutMapping
    public ResponseEntity<PracticeSetItem> updateQuestionSetItem(@RequestBody PracticeSetItem practiceSetItem) {
        PracticeSetItem updatedPracticeSetItem = practiceSetItemService.updateQuestionSetItem(practiceSetItem);
        return ResponseEntity.ok(updatedPracticeSetItem);}

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQuestionSetItem(@PathVariable int id) {
        practiceSetItemService.deleteQuestionSetItem(id);
        return ResponseEntity.noContent().build();}

    @PostMapping("create-items")
    public ResponseEntity<List<PracticeSetItem>> createQuestionSetItems(@RequestParam int easy, @RequestParam int medium, @RequestParam int hard, @RequestBody PracticeSet practiceSet) {
        List<PracticeSetItem> practiceSetItems = practiceSetItemService.createQuestionSetItems(easy, medium, hard, practiceSet);
    return ResponseEntity.ok(practiceSetItems);}
}
