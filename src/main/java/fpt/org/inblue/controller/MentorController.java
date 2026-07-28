package fpt.org.inblue.controller;

import fpt.org.inblue.model.dto.request.ChangeMentorPasswordRequest;
import fpt.org.inblue.model.dto.request.CreateMentorRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorRequest;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.service.MentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mentors")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MentorController {
    private final MentorService mentorService;

    @GetMapping
    public ResponseEntity<List<MentorResponse>> getAllMentors() {
        return ResponseEntity.ok(mentorService.getAllMentors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorResponse> getMentorById(@PathVariable int id) {
        return ResponseEntity.ok(mentorService.getMentorById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Tạo mới Mentor (có input password, trả về MentorResponse không có password)",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            encoding = {@Encoding(name = "data", contentType = "application/json")})))
    public ResponseEntity<MentorResponse> createMentor(
            @RequestPart("data") CreateMentorRequest data,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar)
            throws IOException {
        MentorResponse createdMentor = mentorService.createMentor(data, avatar);
        return ResponseEntity.ok(createdMentor);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Cập nhật Mentor (không có password trong request body và response)",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            encoding = {@Encoding(name = "data", contentType = "application/json")})))
    public ResponseEntity<MentorResponse> updateMentor(
            @PathVariable int id,
            @RequestPart("data") UpdateMentorRequest data,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar)
            throws IOException {
        MentorResponse updatedMentor = mentorService.updateMentor(id, data, avatar);
        return ResponseEntity.ok(updatedMentor);
    }

    @PutMapping("/{id}/change-password")
    @Operation(summary = "Thay đổi mật khẩu cho Mentor")
    public ResponseEntity<MentorResponse> changePassword(
            @PathVariable int id, @RequestBody ChangeMentorPasswordRequest request) {
        MentorResponse response = mentorService.changePassword(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/toggle/{id}")
    public ResponseEntity<Void> toggleActive(@PathVariable int id) {
        mentorService.toggleActive(id);
        return ResponseEntity.noContent().build();
    }
}
