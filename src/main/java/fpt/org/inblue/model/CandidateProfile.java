package fpt.org.inblue.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String targetRole; // Vd: "Java Backend Developer", "Digital Marketer"

    private String targetLevel; // Vd: "Intern", "Fresher", "Junior"

    @Column(columnDefinition = "TEXT")
    private String introduction; // Phần "About Me" - giúp AI hiểu văn phong/tính cách

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> technicalSkills; // Vd: ["Java Core", "Spring Boot", "MySQL", "Docker"]

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> softSkills; // Vd: ["Teamwork", "Time Management", "English"]

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> tools; // Vd: ["IntelliJ", "Jira", "Canva", "Google Analytics"]

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ProjectDetail> projects;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<WorkExperience> workExperiences;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<EducationEntry> educations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> certifications; // Vd: ["AWS Cloud Practitioner", "IELTS 7.0"]

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> achievements;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectDetail implements Serializable {
        private String name; // Tên dự án / Chiến dịch
        private String description; // Mô tả ngắn gọn mục tiêu
        private String role; // Vị trí. Vd: Backend Dev (IT) hoặc Content Creator (MKT)
        private Integer teamSize;
        private List<String> usedTools;

        private String outcome; // Kết quả. Vd: "App chạy ổn định" (IT) hoặc "Tăng 20% reach" (MKT)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkExperience {
        private String company;
        private String position;
        @JsonProperty("start_date")
        private String startDate;
        @JsonProperty("end_date")
        private String endDate;
        private String description; // Mô tả task chính đã làm
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EducationEntry {
        private String school; // Vd: FPT University
        private String major; // Vd: Software Engineering
        private String degree; // Vd: Bachelor
        private String gpa; // Optional -> AI có thể hỏi về nỗ lực học tập
        @JsonProperty("start_date")
        private String startDate;
        @JsonProperty("end_date")
        private String endDate;
    }
}