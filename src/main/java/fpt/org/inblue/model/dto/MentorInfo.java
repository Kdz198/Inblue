package fpt.org.inblue.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class MentorInfo {
    String name;
    String email;
    String password;
    String bio;
    String expertise;
    int yearsOfExperience;
    String linkedInUrl;
       String currentCompany;
}
