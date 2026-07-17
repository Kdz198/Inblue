package fpt.org.inblue.model.dto.request;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoundSessionRequest {
    private Long applicationDetailId; // ID của ApplicationDetail vòng Mentor Review
    private Timestamp joinTime; // Thời gian bắt đầu hẹn gặp (ứng viên + mentor tự chọn)
    private Integer duration; // Thời lượng buổi họp (phút)
    private boolean offline; // true = offline, false = online (tạo phòng Daily.co)
}
