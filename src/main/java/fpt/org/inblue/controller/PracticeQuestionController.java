package fpt.org.inblue.controller;

import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.service.PracticeQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practice-questions")
@CrossOrigin(origins = "*")
public class PracticeQuestionController {
    @Autowired
    private PracticeQuestionService practiceQuestionService;

    @GetMapping
    public ResponseEntity<List<PracticeQuestion>> getAllQuestions() {
        List<PracticeQuestion> practiceQuestions = practiceQuestionService.getAllQuestions();
        return ResponseEntity.ok(practiceQuestions);
    }
    @GetMapping("{id}")
    public ResponseEntity<PracticeQuestion> getQuestionById(@PathVariable int id) {
        PracticeQuestion practiceQuestion = practiceQuestionService.getQuestionById(id);
        return ResponseEntity.ok(practiceQuestion);
    }
    @GetMapping("by-category-level")
    public ResponseEntity<List<PracticeQuestion>> getQuestionsByCategoryAndLevel(@RequestParam int categoryId, @RequestParam String level) {
        List<PracticeQuestion> practiceQuestions = practiceQuestionService.getQuestionsByCategoryAndLevel(categoryId, level);
        return ResponseEntity.ok(practiceQuestions);
    }

    @PostMapping
    public ResponseEntity<PracticeQuestion> createQuestion(@RequestBody PracticeQuestion practiceQuestion) {
        PracticeQuestion createdPracticeQuestion = practiceQuestionService.createQuestion(practiceQuestion);
        return ResponseEntity.ok(createdPracticeQuestion);
    }
    @PutMapping
    public ResponseEntity<PracticeQuestion> updateQuestion(@RequestBody PracticeQuestion practiceQuestion) {
        PracticeQuestion updatedPracticeQuestion = practiceQuestionService.updateQuestion(practiceQuestion);
        return ResponseEntity.ok(updatedPracticeQuestion);
    }
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable int id) {
        practiceQuestionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("random-by-level")
    public ResponseEntity<List<PracticeQuestion>> getRandomQuestionsByLevel(@RequestParam String level, @RequestParam int count) {
        List<PracticeQuestion> practiceQuestions = practiceQuestionService.getRandomQuestionsByLevel(level, count);
        return ResponseEntity.ok(practiceQuestions);}

    @PostMapping("save-all")
    public ResponseEntity<List<PracticeQuestion>> createQuestionList(@RequestBody List<PracticeQuestion> practiceQuestions) {
        List<PracticeQuestion> createdPracticeQuestions = practiceQuestionService.createQuestionList(practiceQuestions);
        return ResponseEntity.ok(createdPracticeQuestions);
    }
}
