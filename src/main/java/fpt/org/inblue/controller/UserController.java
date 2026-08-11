package fpt.org.inblue.controller;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.UserInfo;
import fpt.org.inblue.model.dto.response.UserResponse;
import fpt.org.inblue.model.dto.response.UserScheduleEventDto;
import fpt.org.inblue.service.UserService;
import fpt.org.inblue.service.UserScheduleService;
import fpt.org.inblue.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserScheduleService userScheduleService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/schedule")
    @Operation(
            summary = "Lấy lịch cá nhân của User hiện tại đang đăng nhập",
            description = "Trả về danh sách sự kiện lịch (Application detail round, Kiosk booking, Mentor 1:1 session) của user đăng nhập hiện tại, "
                    + "chuẩn hóa để render trên giao diện Calendar. Hỗ trợ truyền tham số startDate và endDate để lọc khoảng thời gian.")
    public ResponseEntity<List<UserScheduleEventDto>> getCurrentUserSchedule(
            @Parameter(description = "Thời gian bắt đầu (dạng ISO, ví dụ: 2026-08-01T00:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Thời gian kết thúc (dạng ISO, ví dụ: 2026-08-31T23:59:59)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        int userId = securityUtils.getCurrentUserId();
        List<UserScheduleEventDto> schedule = userScheduleService.getUserSchedule(userId, startDate, endDate);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/{userId}/schedule")
    @Operation(
            summary = "Lấy lịch của User theo ID chỉ định",
            description = "Dành cho Admin/Staff/Reviewer lấy danh sách sự kiện lịch của một User cụ thể. Hỗ trợ truyền startDate và endDate để lọc.")
    public ResponseEntity<List<UserScheduleEventDto>> getUserScheduleById(
            @PathVariable int userId,
            @Parameter(description = "Thời gian bắt đầu (dạng ISO, ví dụ: 2026-08-01T00:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Thời gian kết thúc (dạng ISO, ví dụ: 2026-08-31T23:59:59)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<UserScheduleEventDto> schedule = userScheduleService.getUserSchedule(userId, startDate, endDate);
        return ResponseEntity.ok(schedule);
    }

    @Operation(
            summary =
                    "dùng chung cho create và update user, nếu create thì ko có id còn update thì có id gửi kèm trong json data á")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content =
                    @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                @Encoding(name = "avatar", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }))
    public ResponseEntity<User> createUser(
            @RequestPart("data") UserInfo data, @RequestPart(value = "avatar", required = false) MultipartFile avatar)
            throws IOException {
        User createdUser = userService.createUser(data, avatar);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping(path = "upload-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "hàm này để upload cv và parse cv trả về thằng candidate profile",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                     @Content(
                                             mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                             encoding = {
                                                 @Encoding(name = "userId", contentType = "application/json"),
                                                 @Encoding(name = "applicationId", contentType = "application/json"),
                                                 @Encoding(name = "cvFile", contentType = "application/octet-stream")
                                             })))
    public ResponseEntity<CandidateProfile> uploadCv(
            @Parameter(name = "userId", schema = @Schema(type = "string", example = "1")) @RequestPart("userId")
                    int userId,
            @RequestPart(value = "applicationId", required = false) Long applicationId,
            @RequestPart(value = "cvFile", required = false) MultipartFile cvFile)
            throws IOException {
        return ResponseEntity.ok(userService.upCv(userId, applicationId, cvFile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/find-by-id/{userId}")
    public ResponseEntity<UserResponse> getUserResponseById(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserResponseById(userId));
    }

    @PutMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(@RequestParam String oldPass, @RequestParam String newPass) {
        return ResponseEntity.ok(userService.changePassword(oldPass, newPass));
    }
}
