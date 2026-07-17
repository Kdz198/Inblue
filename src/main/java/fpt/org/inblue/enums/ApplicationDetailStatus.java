package fpt.org.inblue.enums;

public enum ApplicationDetailStatus {
    PENDING, // Ứng viên đang làm bài / đã có mentor, đang chờ ứng viên chọn hình thức phỏng vấn
    AWAITING_MENTOR, // Vòng Mentor Review - đang chờ Admin gán mentor
    SLOT_PICKED, // Ứng viên đã chọn online và tạo phòng họp, đang chờ thực hiện
    SUBMITTED, // Đã nộp bài, hệ thống đang gọi AI
    AI_EVALUATED, // AI đã chấm điểm xong (Đang chờ HR duyệt)
    COMPLETED, // HR đã chốt kết quả (Đóng vòng thi này)
}
