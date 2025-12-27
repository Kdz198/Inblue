package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuestionCategory;
import fpt.org.inblue.service.QuestionCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-categories")
public class QuestionCategoryController {
    @Autowired
    private QuestionCategoryService questionCategoryService;

    @GetMapping
    public ResponseEntity<List<QuestionCategory>> findAll() {
        List<QuestionCategory> questionCategories = questionCategoryService.getAllQuestionCategories();
        return ResponseEntity.ok(questionCategories);
    }
    @GetMapping("{id}")
    public ResponseEntity<QuestionCategory> findById(@PathVariable int id) {
        QuestionCategory questionCategory = questionCategoryService.getQuestionCategory(id);
        return ResponseEntity.ok(questionCategory);
    }

    @GetMapping("major/{majorId}")
    public ResponseEntity<List<QuestionCategory>> findByMajorId(@PathVariable int majorId){
        List<QuestionCategory> questionCategories = questionCategoryService.getQuestionCategoriesByMajor(majorId);
        return ResponseEntity.ok(questionCategories);
    }

    @PostMapping
    public ResponseEntity<QuestionCategory> createQuestionCategory(@RequestBody QuestionCategory questionCategory) {
        QuestionCategory createdQuestionCategory = questionCategoryService.createQuestionCategory(questionCategory);
        return ResponseEntity.ok(createdQuestionCategory);
    }

    @PutMapping
    public ResponseEntity<QuestionCategory> updateQuestionCategory(@RequestBody QuestionCategory questionCategory) {
        QuestionCategory updatedQuestionCategory = questionCategoryService.updateQuestionCategory(questionCategory);
        return ResponseEntity.ok(updatedQuestionCategory);
    }
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQuestionCategory(@PathVariable int id) {
        questionCategoryService.deleteQuestionCategory(id);
        return ResponseEntity.noContent().build();
    }
}
