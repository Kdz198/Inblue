package fpt.org.inblue.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fpt.org.inblue.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Mentor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Role role;
    private boolean isActive;
    private String bio;

    private String avatarUrl;
    private String public_id;

    private String expertise;
    private int yearsOfExperience;
    private String linkedInUrl;
    private String currentCompany;
    private int totalSession;
    private double averageRating;

    private Integer pricePerMinute;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private MentorProfile profileData;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "skill_embedding", columnDefinition = "vector(384)")
    @JsonIgnore
    private float[] skillEmbedding;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MentorProfile {
        private List<String> certifications = new ArrayList<>();
        // Kỹ năng cụ thể — khác với "expertise" (tagline ngắn ở entity chính)
        private List<String> skills = new ArrayList<>();
        private String jobTitle;
        private String education;
        private List<String> languages = new ArrayList<>();
        private String portfolioUrl;
        private String githubUrl;
    }
}