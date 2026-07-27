package fpt.org.inblue.model;

import fpt.org.inblue.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Mentor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String email;
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

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
