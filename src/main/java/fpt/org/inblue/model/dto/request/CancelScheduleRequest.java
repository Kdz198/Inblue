package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ứng viên chủ động huỷ lịch hẹn đã đề xuất (vòng Mentor Review, hình thức ONLINE), dù mentor
 * đã duyệt hay chưa. Lý do là optional, khác với ScheduleDecisionRequest của mentor (bắt buộc khi từ chối).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelScheduleRequest {
    private String reason;
}
