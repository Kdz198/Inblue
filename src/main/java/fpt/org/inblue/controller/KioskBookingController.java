package fpt.org.inblue.controller;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.KioskBooking;
import fpt.org.inblue.model.dto.request.KioskEnterDtoRequest;
import fpt.org.inblue.model.dto.request.PickSlotDtoRequest;
import fpt.org.inblue.model.dto.response.KioskEnterDtoResponse;
import fpt.org.inblue.security.CustomUserDetails;
import fpt.org.inblue.service.KioskBookingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class KioskBookingController {

    private final KioskBookingService bookingService;

    private int getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    @Operation(
            summary = "Ứng viên đặt lịch phỏng vấn AI tại Kiosk",
            description = "Cho phép ứng viên chọn một trạm Kiosk và slot thời gian còn trống để đặt lịch phỏng vấn AI.")
    @PostMapping("/api/kiosk-bookings/pick-slot")
    public ResponseEntity<KioskBooking> pickSlot(@RequestBody PickSlotDtoRequest dto) {
        int userId = getCurrentUserId();
        return ResponseEntity.ok(bookingService.pickSlot(dto, userId));
    }

    @Operation(
            summary = "Hủy/đổi lịch phỏng vấn Kiosk",
            description =
                    "Hủy lịch phỏng vấn hiện tại. Chuyển trạng thái booking thành CANCELLED và reset trạng thái vòng thi ứng tuyển về PENDING để ứng viên có thể chọn lại slot mới.")
    @DeleteMapping("/api/kiosk-bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        int userId = getCurrentUserId();
        bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Kiosk xác thực và vào phòng phỏng vấn",
            description =
                    "Máy Kiosk vật lý gửi sessionKey và kioskId để xác thực. Hệ thống kiểm tra thời gian hợp lệ (±15 phút so với giờ hẹn) và gọi orchestrator để lấy AI Session.")
    @PostMapping("/api/kiosk/enter/{sessionKey}")
    public ResponseEntity<KioskEnterDtoResponse> enterKiosk(@PathVariable String sessionKey) {
        return ResponseEntity.ok(bookingService.enterKiosk(sessionKey));
    }

    @GetMapping("/api/kiosk-bookings/application-detail/{applicationDetailId}")
    public ResponseEntity<KioskBooking> getBookingByApplicationDetailId(@PathVariable Long applicationDetailId) {
        KioskBooking booking = bookingService.findByApplicationDetailId(applicationDetailId);
        return ResponseEntity.ok(booking);
    }
}
