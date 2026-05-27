package fpt.org.inblue.enums;

public enum ApplicationDetailStatus {
    PENDING,       // Ứng viên đang làm bài
    SUBMITTED,     // Đã nộp bài, hệ thống đang gọi AI
    AI_EVALUATED,  // AI đã chấm điểm xong (Đang chờ HR duyệt)
    COMPLETED,     // HR đã chốt kết quả (Đóng vòng thi này)
    ERROR          // Lỗi gọi AI
}