package fpt.org.inblue.controller;

import fpt.org.inblue.model.Major;
import fpt.org.inblue.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/majors")
@CrossOrigin(origins = "*")
public class MajorController {
    @Autowired
    private MajorService questionMajorService;

    @GetMapping
    public ResponseEntity<List<Major>> getAllQuestionMajors() {
        List<Major> majors = questionMajorService.getAllQuestionMajors();
        return ResponseEntity.ok(majors);
    }
    @PostMapping
    public ResponseEntity<Major> createQuestionMajor(Major major) {
        Major createdMajor = questionMajorService.createQuestionMajor(major);
        return ResponseEntity.ok(createdMajor);
    }
    @PutMapping
    public ResponseEntity<Major> updateQuestionMajor(Major major) {
        Major updatedMajor = questionMajorService.updateQuestionMajor(major);
        return ResponseEntity.ok(updatedMajor);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Major> getQuestionMajorById(@PathVariable int id) {
        Major major = questionMajorService.getQuestionMajorById(id);
        return ResponseEntity.ok(major);}
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteQuestionMajor(@PathVariable int id) {
        return  ResponseEntity.ok(questionMajorService.deleteQuestionMajor(id));
        }
}

