package fpt.org.inblue.model.dto.request;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MentorProfileRequest {
    private List<String> certifications = new ArrayList<>();
    // Kỹ năng cụ thể — khác với "expertise" (tagline ngắn ở entity chính)
    private List<String> skills = new ArrayList<>();
    private String jobTitle;
    private String education;
    private List<String> languages = new ArrayList<>();
    private String portfolioUrl;
    private String githubUrl;
}
