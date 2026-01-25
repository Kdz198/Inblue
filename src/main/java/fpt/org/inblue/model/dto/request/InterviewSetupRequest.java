package fpt.org.inblue.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSetupRequest {

    // 1. User ID (Bắt buộc để biết session của ai)
    // Trong thực tế có thể lấy từ Token, nhưng MVP FE gửi lên cho lẹ
    @JsonProperty("user_id")
    private String userId;

    // 2. CV Snapshot (Đã parse xong ở bước trước)
    // Lưu ý: Frontend gửi nguyên cục JSON CV đã parse vào đây
    @JsonProperty("candidate_profile")
    private Object candidateProfile;

    // 3. JD Snapshot (Đã parse xong ở bước trước)
    // Dùng lại class static bên trong OrchestratorRequest cho tiện
    @JsonProperty("job_requirement")
    private OrchestratorRequest.JobRequirementData jobRequirement;

    // 4. Config mà User chọn trên UI (Mode, Difficulty, Domain...)
    @JsonProperty("session_config")
    private OrchestratorRequest.SessionConfigData sessionConfig;
}