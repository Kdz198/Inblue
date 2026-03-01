package fpt.org.inblue.model;


import fpt.org.inblue.model.dto.request.OrchestratorRequest;
import fpt.org.inblue.model.dto.response.InterviewBlueprintResponse;
import fpt.org.inblue.model.enums.InterviewEnums;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;

    private String sessionKey; // UUID sessionKey để liên kết với Redis

    // ========================================================================
    // 1. CÁC CỘT LƯU JSONB (Dùng @JdbcTypeCode)
    // ========================================================================

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Lưu Blueprint
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private InterviewBlueprintResponse blueprint;

    // Lưu CV Snapshot
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private  CandidateProfile candidateProfile;

    // Lưu JD Snapshot
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private OrchestratorRequest.JobRequirementData jobRequirement;

    // Lưu Config Snapshot
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private OrchestratorRequest.SessionConfigData sessionConfig;

    // ========================================================================
    // 2. TRẠNG THÁI & INDEXING
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private InterviewEnums.InterviewMode mode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InterviewEnums.JobDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SessionStatus status;

    // ========================================================================
    // 3. TIME
    // ========================================================================

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum SessionStatus {
        CREATED, IN_PROGRESS, COMPLETED, CANCELLED
    }


    // ========================================================================
    // KẾT QUẢ BUỔI PHỎNG VẤN (Update sau khi Finish)
    // ========================================================================

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Điểm tổng kết (0.0 - 10.0) -> Lưu cột riêng để sau này sort: "Top ứng viên điểm cao"
    @Column(name = "overall_score")
    private Double overallScore;

    // Đánh giá xếp loại: PASSED, FAILED, CONSIDER
    @Enumerated(EnumType.STRING)
    private EvaluationResult result;

    // LƯU CHI TIẾT KẾT QUẢ (JSONB)
    // Chứa: List câu hỏi, câu trả lời, feedback từng câu, feedback tổng quan
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private InterviewResultDetail resultDetail;

    public enum EvaluationResult {
        STRONG_HIRE, // >= 9.0: Xuất sắc, offer ngay
        HIRE,        // >= 7.0: Đạt yêu cầu, kiến thức vững
        CONSIDER,    // >= 5.0: Tạm được, cần phỏng vấn thêm hoặc làm vị trí thấp hơn
        REJECT       // < 5.0: Trượt thẳng
    }
}