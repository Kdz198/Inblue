package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mentor duyệt hoặc từ chối lịch hẹn do ứng viên đề xuất (vòng Mentor Review, hình thức ONLINE).
 * Khi từ chối (approved = false) bắt buộc phải có lý do.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDecisionRequest {
    private boolean approved;
    private String reason;
}
