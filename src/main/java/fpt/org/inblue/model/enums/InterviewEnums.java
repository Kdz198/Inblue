package fpt.org.inblue.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class InterviewEnums {

    // ========================================================================
    // 1. CHẾ ĐỘ PHỎNG VẤN (INTERVIEW MODE)
    // ========================================================================
    @Getter
    @AllArgsConstructor
    public enum InterviewMode {
        STANDARD_MOCK(
                "Phỏng vấn tiêu chuẩn (Standard)",
                "Mô phỏng quy trình phỏng vấn thực tế tại doanh nghiệp. Cấu trúc cân bằng giữa Chào hỏi, Kinh nghiệm thực tế (CV) và Kiến thức chuyên môn (JD)."
        ),
        THEORY_CHECK(
                "Dò bài lý thuyết (Theory Check)",
                "Tập trung 90% vào kiểm tra các kiến thức, khái niệm chuyên ngành trong JD (Vd: Nguyên lý Marketing, Quy định kế toán, Luật lao động...). Bỏ qua phần hỏi về dự án cá nhân."
        ),
        PROJECT_DEFENSE(
                "Bảo vệ dự án/Kinh nghiệm (Project Defense)",
                "Tập trung 90% vào xoáy sâu chi tiết các Dự án, Chiến dịch hoặc Kinh nghiệm làm việc đã ghi trong CV. Yêu cầu giải trình về vai trò, kết quả và cách xử lý vấn đề cụ thể."
        );

        private final String label;
        private final String description;
    }

    // ========================================================================
    // 2. ĐỘ KHÓ (DIFFICULTY LEVEL)
    // ========================================================================
    @Getter
    @AllArgsConstructor
    public enum DifficultyLevel {
        FRESHER_BASIC(
                "Cơ bản (Basic)",
                "Câu hỏi tập trung vào định nghĩa, khái niệm và các bước thực hiện cơ bản (Là gì? Gồm những bước nào?). Dành cho ứng viên mới, muốn ôn lại nền tảng."
        ),
        FRESHER_ADVANCED(
                "Nâng cao (Advanced)",
                "Câu hỏi tập trung vào tư duy chiến lược, giải quyết vấn đề và xử lý tình huống khó (Tại sao chọn cách này? Nếu gặp rủi ro X thì làm sao?). Dành cho ứng viên muốn thử thách."
        );

        private final String label;
        private final String description;
    }

    // ========================================================================
    // 3. NGÔN NGỮ (LANGUAGE)
    // ========================================================================
    @Getter
    @AllArgsConstructor
    public enum Language {
        VI("Tiếng Việt", "Phỏng vấn hoàn toàn bằng tiếng Việt."),
        EN("Tiếng Anh", "Phỏng vấn hoàn toàn bằng tiếng Anh (English).");

        private final String label;
        private final String description;
    }

    @Getter
    @AllArgsConstructor
    public enum JobDomain {
        IT("Công nghệ thông tin (IT)", "Dành cho Dev, Tester, QA, DevOps..."),
        NON_IT("Kinh tế & Nghiệp vụ", "Dành cho Marketing, Sales, HR, BA, Kế toán...");

        private final String label;
        private final String description;
    }
}