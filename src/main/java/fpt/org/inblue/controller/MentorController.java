package fpt.org.inblue.controller;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.MentorInfo;
import fpt.org.inblue.service.MentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/mentors")
@CrossOrigin(origins = "*")
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "dùng chung cho create và update mentor, nếu create thì ko có id còn update thì có id gửi kèm trong json data á",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "data", contentType = "application/json") 
                            }
                    )
            )
    )
    public ResponseEntity<Mentor> createMentor(@RequestPart("data") MentorInfo data,
                                               @RequestPart(value = "avatar", required = false) MultipartFile avatar,
                                               @RequestPart(value = "identityFile", required = false) MultipartFile identityFile,
                                               @RequestPart(value = "degreeFile", required = false) MultipartFile degreeFile,
                                               @RequestPart(value = "otherFile", required = false) MultipartFile otherFile) throws IOException {
        Mentor createdMentor = mentorService.createMentor(data, identityFile, degreeFile, otherFile, avatar);
        return ResponseEntity.ok(createdMentor);
    }

    @PutMapping
    public ResponseEntity<Mentor> updateMentor(@RequestBody Mentor mentor) {
        Mentor updatedMentor = mentorService.updateMentor(mentor);
        return ResponseEntity.ok(updatedMentor);
    }

    @GetMapping("/toggle/{id}")
    public ResponseEntity<Void> toggleActive(@PathVariable int id) {
        mentorService.toggleActive(id);
        return ResponseEntity.noContent().build();
           }
}
