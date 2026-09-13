package fpt.org.inblue.model.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class MentorProfileResponse {
    List<String> certifications;
    List<String> skills;
    String jobTitle;
    String education;
    List<String> languages;
    String portfolioUrl;
    String githubUrl;
}
