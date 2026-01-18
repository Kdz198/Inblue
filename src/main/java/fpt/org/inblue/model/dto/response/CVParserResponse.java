package fpt.org.inblue.model.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CVParserResponse {

    private String targetRole;
    private String targetLevel;
    private String introduction;

    private List<String> technicalSkills;
    private List<String> softSkills;
    private List<String> tools;
    private List<String> certifications;
    private List<String> achievements;

    private List<EducationDTO> educations;
    private List<WorkExperienceDTO> workExperiences;
    private List<ProjectDTO> projects;

    @Data
    public static class EducationDTO {
        private String school;
        private String major;
        private String degree;
        private String gpa;

        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("end_date")
        private String endDate;
    }

    @Data
    public static class WorkExperienceDTO {
        private String company;
        private String position;
        private String description;

        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("end_date")
        private String endDate;
    }

    @Data
    public static class ProjectDTO {
        private String name;
        private String description;
        private String role;
        private Integer teamSize;
        private List<String> usedTools;
        private String outcome;
    }
}