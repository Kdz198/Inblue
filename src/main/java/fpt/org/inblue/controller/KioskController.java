package fpt.org.inblue.controller;

import fpt.org.inblue.model.Kiosk;
import fpt.org.inblue.model.KioskSchedule;
import fpt.org.inblue.model.dto.SlotDto;
import fpt.org.inblue.model.dto.response.KioskHistoryResponseDto;
import fpt.org.inblue.service.KioskService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/kiosks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class KioskController {

    private final KioskService kioskService;

    @Operation(
            summary = "Lấy danh sách các Kiosk ",
            description = "Trả về danh sách tất cả các trạm Kiosk vật lý có trạng thái hoạt động (isActive = true).")
    @GetMapping
    public ResponseEntity<List<Kiosk>> getAllKiosk() {
        return ResponseEntity.ok(kioskService.getAllKiosk());
    }

    @Operation(
            summary = "Lấy danh sách các slot trống của Kiosk",
            description =
                    "Tính toán và trả về danh sách các khung giờ (slot) còn trống của trạm Kiosk được chọn trong một ngày cụ thể (bao gồm khoảng nghỉ 15 phút giữa các slot).")
    @GetMapping("/{kioskId}/slots")
    public ResponseEntity<List<SlotDto>> getAvailableSlots(
            @PathVariable Long kioskId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(kioskService.getAvailableSlots(kioskId, date));
    }

    @Operation(
            summary = "Tạo một Kiosk mới",
            description = "Tạo cấu hình cho một máy trạm Kiosk vật lý mới trong hệ thống (chỉ dành cho Admin).")
    @PostMapping
    public ResponseEntity<Kiosk> createKiosk(@RequestBody Kiosk kiosk) {
        return ResponseEntity.ok(kioskService.createKiosk(kiosk));
    }

    @Operation(
            summary = "Thiết lập lịch hoạt động định kỳ của Kiosk",
            description =
                    "Tạo cấu hình lịch làm việc (giờ mở cửa, đóng cửa, độ dài slot) định kỳ theo thứ trong tuần của một Kiosk (chỉ dành cho Admin).")
    @PostMapping("/schedule")
    public ResponseEntity<KioskSchedule> createSchedule(@RequestBody KioskSchedule schedule) {
        return ResponseEntity.ok(kioskService.createSchedule(schedule));
    }

    @Operation(
            summary = "Lấy danh sách lịch hoạt động định kỳ của Kiosk",
            description = "Trả về danh sách các cấu hình lịch làm việc theo thứ của một Kiosk.")
    @GetMapping("/{kioskId}/schedules")
    public ResponseEntity<List<KioskSchedule>> getSchedulesByKiosk(@PathVariable Long kioskId) {
        return ResponseEntity.ok(kioskService.getSchedulesByKiosk(kioskId));
    }

    @Operation(
            summary = "Cập nhật thông tin Kiosk",
            description =
                    "Chỉnh sửa tên, vị trí hoặc trạng thái hoạt động của một Kiosk vật lý hiện có (chỉ dành cho Admin).")
    @PutMapping("/{id}")
    public ResponseEntity<Kiosk> updateKiosk(@PathVariable Long id, @RequestBody Kiosk kiosk) {
        return ResponseEntity.ok(kioskService.updateKiosk(id, kiosk));
    }

    @Operation(
            summary = "Cập nhật lịch hoạt động định kỳ của Kiosk",
            description =
                    "Chỉnh sửa thông số lịch làm việc định kỳ (giờ mở, giờ đóng, độ dài slot) của Kiosk theo mã lịch (chỉ dành cho Admin).")
    @PutMapping("/schedule/{id}")
    public ResponseEntity<KioskSchedule> updateSchedule(@PathVariable Long id, @RequestBody KioskSchedule schedule) {
        return ResponseEntity.ok(kioskService.updateSchedule(id, schedule));
    }

    @Operation(
            summary = "Lấy lịch sử tham gia/đặt lịch của trạm Kiosk",
            description = "Trả về toàn bộ danh sách lịch sử đặt lịch và sử dụng phỏng vấn của trạm Kiosk chọn, bao gồm thông tin ứng viên và trạng thái đặt lịch."
    )
    @GetMapping("/{kioskId}/history")
    public ResponseEntity<List<KioskHistoryResponseDto>> getKioskHistory(@PathVariable Long kioskId) {
        return ResponseEntity.ok(kioskService.getKioskHistory(kioskId));
    }
}
