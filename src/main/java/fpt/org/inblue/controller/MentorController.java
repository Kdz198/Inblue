package fpt.org.inblue.controller;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.dto.request.ChangeMentorPasswordRequest;
import fpt.org.inblue.model.dto.request.CreateMentorRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorRequest;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.model.dto.response.UserScheduleEventDto;
import fpt.org.inblue.service.MentorService;
import fpt.org.inblue.service.UserScheduleService;
import fpt.org.inblue.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
    private final UserScheduleService userScheduleService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<MentorResponse>> getAllMentors() {
        return ResponseEntity.ok(mentorService.getAllMentors());
    }

    @GetMapping("/schedule")
    @Operation(
            summary = "Lấy lịch cá nhân của Mentor hiện tại đang đăng nhập",
            description =
                    "Trả về danh sách sự kiện lịch (Application detail round được phân công, Mentor 1:1 session, Kiosk booking liên quan) của mentor đăng nhập hiện tại. Hỗ trợ truyền tham số startDate và endDate để lọc khoảng thời gian.")
    public ResponseEntity<List<UserScheduleEventDto>> getCurrentMentorSchedule(
            @Parameter(description = "Thời gian bắt đầu (dạng ISO, ví dụ: 2026-08-01T00:00:00)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @Parameter(description = "Thời gian kết thúc (dạng ISO, ví dụ: 2026-08-31T23:59:59)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {

        int mentorId = securityUtils.getCurrentUserId();
        List<UserScheduleEventDto> schedule = userScheduleService.getMentorSchedule(mentorId, startDate, endDate);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/{mentorId}/schedule")
    @Operation(
            summary = "Lấy lịch của Mentor theo ID chỉ định",
            description =
                    "Dành cho Admin/Staff/User lấy danh sách sự kiện lịch của một Mentor cụ thể. Hỗ trợ truyền startDate và endDate để lọc.")
    public ResponseEntity<List<UserScheduleEventDto>> getMentorScheduleById(
            @PathVariable int mentorId,
            @Parameter(description = "Thời gian bắt đầu (dạng ISO, ví dụ: 2026-08-01T00:00:00)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @Parameter(description = "Thời gian kết thúc (dạng ISO, ví dụ: 2026-08-31T23:59:59)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {

        List<UserScheduleEventDto> schedule = userScheduleService.getMentorSchedule(mentorId, startDate, endDate);
        return ResponseEntity.ok(schedule);
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
