package fpt.org.inblue.model.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MentorResponse {
    int id;
    String name;
    String email;
    boolean isActive;
    String bio;
    String avatarUrl;
    String expertise;
    int yearsOfExperience;
    String linkedInUrl;
    String currentCompany;
    int rate;
    int totalSession;
    double averageRating;
    int pricePerMinute;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
