package fpt.org.inblue.enums;

public enum RoundType {
    CV_SCREENING,          // Lọc CV (Chấm Match Score, Keyword)
    EMAIL_SIMULATOR,       // Giao tiếp chuyên nghiệp (Báo cáo sếp, từ chối task)
    QUIZ,                  // Trắc nghiệm kiến thức (Core, DB, Framework)
    CODING,     // Thuật toán / Logic
    CODE_REVIEW,           // Đọc hiểu & Bắt lỗi code người khác
    MENTROR_REVIEW,          // phỏng vấn với mentor (có phần đánh giá mentor)
    AI_INTERVIEW           // Vấn đáp trực tiếp (Tech + Behavioral)
}