package fpt.org.inblue.enums;

public enum ApplicationStatus {
    IN_PROGRESS,  // Đang thi (Mới apply hoặc đang ở giữa các vòng)
    PASSED,       // Vượt qua tất cả các vòng (Đậu)
    FAILED,       // Tạch (Và kết thúc, không chơi nữa)
    SOFT_FAILED   // Tạch nhưng vẫn đang chơi tiếp ở chế độ trải nghiệm
}