package fpt.org.inblue.controller;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.model.dto.request.AdminJdApplicationsResponseDto;
import fpt.org.inblue.model.dto.response.admin.AdminApplicationDetailResponse;
import fpt.org.inblue.model.dto.response.admin.AdminApplicationFullDetailResponseDto;
import fpt.org.inblue.model.dto.response.admin.AdminApplicationsPerUserResponse;
import fpt.org.inblue.model.dto.response.admin.AdminDashboardOverviewResponse;
import fpt.org.inblue.model.dto.response.admin.AdminOpenJdResponseDto;
import fpt.org.inblue.service.AdminManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(
        name = "Admin Management",
        description = "APIs phục vụ các tác vụ quản trị cho Admin (Quản lý JD mở, lượt apply & chi tiết application)")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    @GetMapping("/open-jds")
    @Operation(
            summary = "Lấy danh sách các JD theo công ty (Hỗ trợ lọc theo trạng thái hoặc lấy tất cả)",
            description =
                    "Trả về danh sách các Job Description kèm thông tin công ty sở hữu và thống kê lượt ứng tuyển. Nếu truyền query param `status` (VD: OPEN, CLOSED, DRAFT) thì lọc theo trạng thái đó, nếu để trống/null thì lấy tất cả các trạng thái (ALL).")
    public ResponseEntity<List<AdminOpenJdResponseDto>> getOpenJds(
            @RequestParam(required = false) JobDescriptionStatus status) {
        List<AdminOpenJdResponseDto> response = adminManagementService.getOpenJdsWithCompanyAndStats(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jds/{jdId}/applications")
    @Operation(
            summary = "Lấy danh sách các lượt apply của một JD cụ thể",
            description =
                    "Trả về danh sách các ứng viên đã ứng tuyển vào JD được chọn, thông tin ứng viên, điểm số tổng quan và vòng thi hiện tại.")
    public ResponseEntity<AdminJdApplicationsResponseDto> getApplicationsByJdId(@PathVariable Long jdId) {
        AdminJdApplicationsResponseDto response = adminManagementService.getApplicationsByJdId(jdId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applications/{applicationId}/detail")
    @Operation(
            summary = "Lấy chi tiết sâu toàn diện của một đơn ứng tuyển (Application Detail)",
            description =
                    "Trả về bức tranh toàn cảnh của 1 ứng viên: Hồ sơ cá nhân (Candidate Profile), CV link, JD info và kết quả từng vòng thi (điểm AI, AI feedback, điểm HR, HR note, kết quả final, submission data).")
    public ResponseEntity<AdminApplicationFullDetailResponseDto> getApplicationFullDetail(
            @PathVariable Long applicationId) {
        AdminApplicationFullDetailResponseDto response = adminManagementService.getApplicationFullDetail(applicationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/application-details")
    @Operation(
            summary = "Lấy danh sách các vòng thi (Application Details) và hỗ trợ lọc theo status",
            description =
                    "Trả về danh sách các Application Details. Hỗ trợ lọc theo trạng thái (ví dụ: status=AWAITING_MENTOR). Nếu không truyền status thì trả về tất cả.")
    public ResponseEntity<List<AdminApplicationDetailResponse>> getApplicationDetails(
            @RequestParam(required = false) ApplicationDetailStatus status) {
        List<AdminApplicationDetailResponse> response = adminManagementService.getApplicationDetails(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/overview")
    @Operation(
            summary = "Lấy dữ liệu tổng quan cho Admin Dashboard",
            description =
                    "Trả về thống kê lượt ứng tuyển, top job/vị trí được quan tâm, interview đang diễn ra và tiến độ theo từng vòng. "
                            + "Tham số limit mặc định là 10, tối đa 50. days là số ngày lấy giao dịch gần đây, mặc định 7 ngày.")
    public ResponseEntity<AdminDashboardOverviewResponse> getDashboardOverview(
            @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(adminManagementService.getDashboardOverview(limit, days));
    }

    @GetMapping("/analytics/applications-per-user")
    @Operation(
            summary = "Lấy số lượt apply trung bình trên mỗi user",
            description =
                    "Thống kê trên các application chưa bị xoá, bao gồm tổng lượt apply, số user đã apply và trung bình lượt apply/user.")
    public ResponseEntity<AdminApplicationsPerUserResponse> getApplicationsPerUser() {
        return ResponseEntity.ok(adminManagementService.getApplicationsPerUser());
    }
}
