package fpt.org.inblue.controller;

import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.dto.request.CreateMentorReviewRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorReviewRequest;
import fpt.org.inblue.service.MentorReviewService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/mentor-reviews")
public class MentorReviewController {
    @Autowired
    private MentorReviewService mentorReviewService;

    @GetMapping
    public ResponseEntity<List<MentorReview>> getAllMentorReviews() {
        List<MentorReview> mentorReviews = mentorReviewService.getAllMentorReviews();
        return ResponseEntity.ok(mentorReviews);
    }


    @PostMapping
    public ResponseEntity<MentorReview> createMentorReview(@RequestBody CreateMentorReviewRequest mentorReview) {
        MentorReview createdMentorReview = mentorReviewService.mentorReview(mentorReview);
        return ResponseEntity.ok(createdMentorReview);
    }

    @Operation(summary = "Json mẫu update ( update vs create khác json", description = "{\n" +
            "  \"id\": 1,\n" +
            "  \"rating\": 4,\n" +
            "  \"situationNote\": \"string\",\n" +
            "  \"taskNote\": \"string\",\n" +
            "  \"actionNote\": \"string\",\n" +
            "  \"resultNote\": \"string\",\n" +
            "  \"strength\": \"string\",\n" +
            "  \"weakness\": \"string\",\n" +
            "  \"improve\": \"string\"\n" +
            "}")
    @PutMapping
    public ResponseEntity<MentorReview> updateMentorReview(@RequestBody UpdateMentorReviewRequest mentorReview) {
        MentorReview updatedMentorReview = mentorReviewService.updateMentorReview(mentorReview);
        return ResponseEntity.ok(updatedMentorReview);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorReview> getMentorReviewById(@PathVariable int id) {
        MentorReview mentorReview = mentorReviewService.getMentorReviewById(id);
        return ResponseEntity.ok(mentorReview);}
}
