package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuestionLesson;
import fpt.org.inblue.service.QuestionLessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-categories")
@CrossOrigin(origins = "*")
public class QuestionLessonController {
    @Autowired
    private QuestionLessonService questionLessonService;

    @GetMapping
    public ResponseEntity<List<QuestionLesson>> findAll() {
        List<QuestionLesson> questionCategories = questionLessonService.getAllQuestionCategories();
        return ResponseEntity.ok(questionCategories);
    }
    @GetMapping("{id}")
    public ResponseEntity<QuestionLesson> findById(@PathVariable int id) {
        QuestionLesson questionLesson = questionLessonService.getQuestionCategory(id);
        return ResponseEntity.ok(questionLesson);
    }


    @PostMapping
    public ResponseEntity<QuestionLesson> createQuestionCategory(@RequestBody QuestionLesson questionLesson) {
        QuestionLesson createdQuestionLesson = questionLessonService.createQuestionCategory(questionLesson);
        return ResponseEntity.ok(createdQuestionLesson);
    }

    @PutMapping
    public ResponseEntity<QuestionLesson> updateQuestionCategory(@RequestBody QuestionLesson questionLesson) {
        QuestionLesson updatedQuestionLesson = questionLessonService.updateQuestionCategory(questionLesson);
        return ResponseEntity.ok(updatedQuestionLesson);
    }
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQuestionCategory(@PathVariable int id) {
        questionLessonService.deleteQuestionCategory(id);
        return ResponseEntity.noContent().build();
    }
}
