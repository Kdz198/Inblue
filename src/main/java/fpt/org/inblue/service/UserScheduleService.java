package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.response.UserScheduleEventDto;
import java.time.LocalDateTime;
import java.util.List;

public interface UserScheduleService {
    /**
     * Lấy danh sách tất cả các sự kiện lịch (Application round, Kiosk booking, Mentor session) của User,
     * có hỗ trợ lọc theo khoảng thời gian startDate và endDate.
     */
    List<UserScheduleEventDto> getUserSchedule(int userId, LocalDateTime startDate, LocalDateTime endDate);
}
