package fpt.org.inblue.controller;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.CreateMentorRequest;
import fpt.org.inblue.service.MentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentors")
public class MentorController {
    @Autowired
    private MentorService mentorService;

    @GetMapping
    public ResponseEntity<List<Mentor>> getAllMentors() {
        List<Mentor> mentors = mentorService.getAllMentors();
        return ResponseEntity.ok(mentors);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Mentor> getMentorById(@PathVariable int id) {
        Mentor mentor = mentorService.getMentorById(id);
        return ResponseEntity.ok(mentor);
    }

    @PostMapping
    public ResponseEntity<Mentor> createMentor(@RequestBody CreateMentorRequest mentor) {
        Mentor createdMentor = mentorService.createMentor(mentor);
        return ResponseEntity.ok(createdMentor);
    }

    @PutMapping
    public ResponseEntity<Mentor> updateMentor(@RequestBody Mentor mentor) {
        Mentor updatedMentor = mentorService.updateMentor(mentor);
        return ResponseEntity.ok(updatedMentor);
    }
}
