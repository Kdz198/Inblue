package fpt.org.inblue.model.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MentorInfo {
    @Nullable
    Integer id;

    String name;
    String email;
    String password;
    String bio;
    String expertise;
    int yearsOfExperience;
    String linkedInUrl;
    String currentCompany;
    int pricePerMinute;

    public MentorInfo(
            String name,
            String email,
            String password,
            String bio,
            String expertise,
            int yearsOfExperience,
            String linkedInUrl,
            String currentCompany,
            int pricePerMinute) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.expertise = expertise;
        this.yearsOfExperience = yearsOfExperience;
        this.linkedInUrl = linkedInUrl;
        this.currentCompany = currentCompany;
        this.pricePerMinute = pricePerMinute;
    }
}
