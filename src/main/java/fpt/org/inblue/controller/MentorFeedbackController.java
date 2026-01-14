package fpt.org.inblue.controller;

import fpt.org.inblue.model.MentorFeedback;
import fpt.org.inblue.model.dto.request.CreateMentorFeedbackRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorFeedbackRequest;
import fpt.org.inblue.service.MentorFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/mentor-feedbacks")
@CrossOrigin(origins = "*")
public class MentorFeedbackController {
    @Autowired
    private MentorFeedbackService mentorFeedbackService;

    @GetMapping("{id}")
    public ResponseEntity<MentorFeedback> getMentorFeedbackBySessionId(@PathVariable int id) {
        MentorFeedback mentorFeedback = mentorFeedbackService.getMentorFeedbackBySessionId(id);
        return ResponseEntity.ok(mentorFeedback);
    }

    @GetMapping()
    public ResponseEntity<java.util.List<MentorFeedback>> getAllMentorFeedbacks() {
        List<MentorFeedback> mentorFeedbacks = mentorFeedbackService.getAllMentorFeedbacks();
        return ResponseEntity.ok(mentorFeedbacks);
    }

    @GetMapping("mentor/{mentorId}")
    public ResponseEntity<List<MentorFeedback>> getAllByMentor(@PathVariable int mentorId) {
        List<MentorFeedback> mentorFeedbacks = mentorFeedbackService.getAllByMentor(mentorId);
        return ResponseEntity.ok(mentorFeedbacks);}

    @PostMapping
    public ResponseEntity<MentorFeedback> createMentorFeedback(@RequestBody CreateMentorFeedbackRequest mentorFeedback) {
        MentorFeedback createdMentorFeedback = mentorFeedbackService.createMentorFeedback(mentorFeedback);
        return ResponseEntity.ok(createdMentorFeedback);
    }
    @PutMapping
    public ResponseEntity<MentorFeedback> updateMentorFeedback(@RequestBody UpdateMentorFeedbackRequest mentorFeedback) {
        MentorFeedback updatedMentorFeedback = mentorFeedbackService.updateMentorFeedback(mentorFeedback);
        return ResponseEntity.ok(updatedMentorFeedback);    }
}
