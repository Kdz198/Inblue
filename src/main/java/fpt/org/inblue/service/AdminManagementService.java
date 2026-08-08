package fpt.org.inblue.service;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.model.dto.request.AdminJdApplicationsResponseDto;
import fpt.org.inblue.model.dto.response.admin.AdminApplicationDetailResponse;
import fpt.org.inblue.model.dto.response.admin.AdminApplicationFullDetailResponseDto;
import fpt.org.inblue.model.dto.response.admin.AdminApplicationsPerUserResponse;
import fpt.org.inblue.model.dto.response.admin.AdminDashboardOverviewResponse;
import fpt.org.inblue.model.dto.response.admin.AdminOpenJdResponseDto;
import java.util.List;

public interface AdminManagementService {

    /**
     * Lấy danh sách các Job Description đang mở (hoặc theo status truyền vào)
     * kèm thông tin Công ty sở hữu và thống kê số lượt ứng tuyển.
     */
    List<AdminOpenJdResponseDto> getOpenJdsWithCompanyAndStats(JobDescriptionStatus status);

    /**
     * Lấy danh sách tất cả ứng viên / lượt apply của một Job Description cụ thể.
     */
    AdminJdApplicationsResponseDto getApplicationsByJdId(Long jdId);

    /**
     * Lấy thông tin chi tiết toàn diện của một hồ sơ ứng tuyển (Application),
     * bao gồm thông tin ứng viên, Candidate Profile, JD và chi tiết từng vòng thi (ApplicationDetail).
     */
    AdminApplicationFullDetailResponseDto getApplicationFullDetail(Long applicationId);

    /**
     * Lấy danh sách Application Details và hỗ trợ lọc theo status.
     */
    List<AdminApplicationDetailResponse> getApplicationDetails(ApplicationDetailStatus status);

    /**
     * Lấy dữ liệu tổng hợp cho dashboard Admin: xu hướng ứng tuyển, trạng thái hồ sơ,
     * interview đang diễn ra và tiến độ theo từng vòng.
     */
    AdminDashboardOverviewResponse getDashboardOverview(int limit);

    AdminDashboardOverviewResponse getDashboardOverview(int limit, int days);

    AdminApplicationsPerUserResponse getApplicationsPerUser();
}
