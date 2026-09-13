package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMentorRequest {
    private String name;
    private String email;
    private String password;
    private String bio;
    private String expertise;
    private int yearsOfExperience;
    private String linkedInUrl;
    private String currentCompany;
    private Integer pricePerMinute;
    private MentorProfileRequest profileData;
}
