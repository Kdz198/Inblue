package fpt.org.inblue.controller;

import fpt.org.inblue.model.Question;
import fpt.org.inblue.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }
    @GetMapping("{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable int id) {
        Question question = questionService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }
    @GetMapping("by-category-level")
    public ResponseEntity<List<Question>> getQuestionsByCategoryAndLevel(@RequestParam int categoryId, @RequestParam String level) {
        List<Question> questions = questionService.getQuestionsByCategoryAndLevel(categoryId, level);
        return ResponseEntity.ok(questions);
    }

    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody Question question) {
        Question createdQuestion = questionService.createQuestion(question);
        return ResponseEntity.ok(createdQuestion);
    }
    @PutMapping
    public ResponseEntity<Question> updateQuestion(@RequestBody Question question) {
        Question updatedQuestion = questionService.updateQuestion(question);
        return ResponseEntity.ok(updatedQuestion);
    }
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable int id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("random-by-level")
    public ResponseEntity<List<Question>> getRandomQuestionsByLevel(@RequestParam String level, @RequestParam int count) {
        List<Question> questions = questionService.getRandomQuestionsByLevel(level, count);
        return ResponseEntity.ok(questions);}

    @PostMapping("save-all")
    public ResponseEntity<List<Question>> createQuestionList(@RequestBody List<Question> questions) {
        List<Question> createdQuestions = questionService.createQuestionList(questions);
        return ResponseEntity.ok(createdQuestions);
    }
}
