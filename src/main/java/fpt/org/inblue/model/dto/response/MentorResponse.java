package fpt.org.inblue.model.dto.response;

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
     String identityImg;
     String degreeImg;
     String otherFile;
     int totalSession;
     double averageRating;
}
