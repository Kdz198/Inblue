package fpt.org.inblue.model.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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

    public MentorInfo(String name, String email, String password, String bio, String expertise, int yearsOfExperience, String linkedInUrl, String currentCompany) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.expertise = expertise;
        this.yearsOfExperience = yearsOfExperience;
        this.linkedInUrl = linkedInUrl;
        this.currentCompany = currentCompany;
    }
}
