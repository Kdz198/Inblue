package fpt.org.inblue.service;

import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.response.MentorResponse;
import fpt.org.inblue.model.dto.response.ReviewerApplicationDetailResponseDto;
import java.util.List;

public interface ApplicationDetailService {
    ApplicationDetail getApplicationDetailById(long id);

    List<ApplicationDetail> getByApplicationId(long applicationId);

    void hrScore(long applicationId, boolean isPass, String note, double score);

    List<ReviewerApplicationDetailResponseDto> getApplicationDetailsForReviewer();

    // Dành cho Admin: gán 1 mentor cho vòng Mentor Review
    ApplicationDetail assignMentor(long applicationDetailId, int mentorId);

    // Dành cho Admin: gán danh sách nhiều mentor cho vòng Mentor Review
    ApplicationDetail assignMentors(long applicationDetailId, List<Integer> mentorIds);

    // Dành cho Candidate: chọn 1 mentor từ danh sách do Admin đề xuất
    ApplicationDetail selectMentor(long applicationDetailId, int mentorId);

    // Lấy thông tin chi tiết các mentor được đề xuất cho ứng viên
    List<MentorResponse> getAssignedMentors(long applicationDetailId);

    // Bắt đầu vòng AI Interview (gọi từ Kiosk checkin hoặc web)
    String startAiInterview(long applicationDetailId);
}
