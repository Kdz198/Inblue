package fpt.org.inblue.controller;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.MentorInterviewBooking;
import fpt.org.inblue.model.dto.request.AssignMentorDtoRequest;
import fpt.org.inblue.model.dto.request.KioskEnterDtoRequest;
import fpt.org.inblue.model.dto.request.PickSlotDtoRequest;
import fpt.org.inblue.model.dto.response.KioskEnterDtoResponse;
import fpt.org.inblue.security.CustomUserDetails;
import fpt.org.inblue.service.MentorInterviewBookingService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MentorInterviewBookingController {

    private final MentorInterviewBookingService bookingService;

    private int getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    @Operation(
            summary = "Ứng viên đặt lịch phỏng vấn tại Kiosk",
            description =
                    "Cho phép ứng viên chọn một trạm Kiosk và slot thời gian còn trống để đặt lịch phỏng vấn (tạo booking trạng thái AWAITING_MENTOR).")
    @PostMapping("/api/mentor-bookings/pick-slot")
    public ResponseEntity<MentorInterviewBooking> pickSlot(@RequestBody PickSlotDtoRequest dto) {
        int userId = getCurrentUserId();
        return ResponseEntity.ok(bookingService.pickSlot(dto, userId));
    }

    @Operation(
            summary = "Hủy/đổi lịch phỏng vấn",
            description =
                    "Hủy lịch phỏng vấn hiện tại. Chuyển trạng thái booking thành CANCELLED và reset trạng thái vòng thi ứng tuyển về PENDING để ứng viên có thể chọn lại slot mới.")
    @DeleteMapping("/api/mentor-bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        int userId = getCurrentUserId();
        bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Lấy danh sách các lượt phỏng vấn theo trạng thái",
            description =
                    "Lấy toàn bộ các lượt đặt lịch phỏng vấn dựa theo trạng thái lọc (chỉ dành cho Admin/Staff). Mặc định là AWAITING_MENTOR.")
    @GetMapping("/api/admin/mentor-bookings")
    public ResponseEntity<List<MentorInterviewBooking>> getBookingsByStatus(
            @RequestParam(required = false, defaultValue = "AWAITING_MENTOR") BookingStatus status) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
    }

    @Operation(
            summary = "Gán Mentor cho lịch phỏng vấn",
            description =
                    "Admin thực hiện gán Mentor cho một lượt phỏng vấn. Hệ thống kiểm tra trùng lịch Mentor, gọi Daily.co để tạo phòng, sinh mã sessionKey và gửi thông báo cho ứng viên (chỉ dành cho Admin).")
    @PostMapping("/api/admin/mentor-bookings/{bookingId}/assign-mentor")
    public ResponseEntity<MentorInterviewBooking> assignMentor(
            @PathVariable Long bookingId, @RequestBody AssignMentorDtoRequest dto) {
        return ResponseEntity.ok(bookingService.assignMentor(bookingId, dto.getMentorId(), dto.getNotes()));
    }

    @Operation(
            summary = "Kiosk xác thực và vào phòng phỏng vấn",
            description =
                    "Máy Kiosk vật lý gửi sessionKey và kioskId để xác thực. Hệ thống kiểm tra thời gian hợp lệ (±15 phút so với giờ hẹn) và sinh Daily.co meetingToken cho ứng viên.")
    @PostMapping("/api/kiosk/enter")
    public ResponseEntity<KioskEnterDtoResponse> enterKiosk(@RequestBody KioskEnterDtoRequest dto) {
        return ResponseEntity.ok(bookingService.enterKiosk(dto.getSessionKey(), dto.getKioskId()));
    }
}
