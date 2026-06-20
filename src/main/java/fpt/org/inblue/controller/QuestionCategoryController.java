package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuestionCategory;
import fpt.org.inblue.service.QuestionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/question-categories")
public class QuestionCategoryController {
    @Autowired
    private QuestionCategoryService questionCategoryService;

    @GetMapping
    public ResponseEntity<List<QuestionCategory>> getAllQuestionCategories() {
        List<QuestionCategory> questionCategories = questionCategoryService.getAllQuestionCategories();
        return ResponseEntity.ok(questionCategories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionCategory> getQuestionCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(questionCategoryService.getQuestionCategory(id));
    }

    @PostMapping
    public ResponseEntity<QuestionCategory> createQuestionCategory(@RequestBody QuestionCategory questionCategory) {
        return ResponseEntity.ok(questionCategoryService.saveQuestionCategory(questionCategory));
    }

    @PutMapping
    public ResponseEntity<QuestionCategory> updateQuestionCategory( @RequestBody QuestionCategory questionCategory) {
        return ResponseEntity.ok(questionCategoryService.updateQuestionCategory(questionCategory));
    }
    @Operation(summary = "Delete riu")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionCategory(@PathVariable Integer id) {
        questionCategoryService.deleteQuestionCategory(id);
        return ResponseEntity.noContent().build();}
}
