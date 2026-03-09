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
    private MajorService MajorService;

    @GetMapping
    public ResponseEntity<List<Major>> getAllMajors() {
        List<Major> majors = MajorService.getAllMajors();
        return ResponseEntity.ok(majors);
    }
    @PostMapping
    public ResponseEntity<Major> createMajor(@RequestBody Major major) {
        Major createdMajor = MajorService.createMajor(major);
        return ResponseEntity.ok(createdMajor);
    }
    @PutMapping
    public ResponseEntity<Major> updateMajor(@RequestBody Major major) {
        Major updatedMajor = MajorService.updateMajor(major);
        return ResponseEntity.ok(updatedMajor);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Major> getMajorById(@PathVariable int id) {
        Major major = MajorService.getMajorById(id);
        return ResponseEntity.ok(major);}
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteMajor(@PathVariable int id) {
        return  ResponseEntity.ok(MajorService.deleteMajor(id));
        }
}

