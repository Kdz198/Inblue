package fpt.org.inblue.controller;


import fpt.org.inblue.model.dto.request.FaceSnapshotRequest;
import fpt.org.inblue.service.ProctoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proctoring")
@RequiredArgsConstructor
@Slf4j // Khuyến khích dùng log để theo dõi request bắn lên liên tục
public class ProctoringController {

    private final ProctoringService proctoringService;

    /**
     * API này được Frontend gọi liên tục (ví dụ: mỗi 2-3 giây)
     * dưới dạng "Fire-and-Forget" (Bắn và Quên) để không làm block UI.
     */
    @PostMapping("/track")
    public ResponseEntity<Void> trackBehavior(@RequestBody FaceSnapshotRequest request) {
        try {
            // Đẩy xuống Service để xử lý (Gọi Python + Lưu Redis nếu có gian lận)
            proctoringService.trackSnapshot(request);

            // Trả về 200 OK ngay lập tức (Không cần body để tiết kiệm băng thông)
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            // Log lỗi nhưng VẪN TRẢ VỀ 200 OK HOẶC 202 ACCEPTED cho FE
            // Tránh việc FE nhận lỗi 500 liên tục làm hỏng luồng phỏng vấn chính
            log.error("Lỗi khi xử lý proctoring snapshot cho session [{}]: {}",
                    request.getSessionKey(), e.getMessage());
            return ResponseEntity.accepted().build();
        }
    }
}