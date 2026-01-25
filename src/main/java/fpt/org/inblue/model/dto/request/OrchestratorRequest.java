package fpt.org.inblue.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.enums.InterviewEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrchestratorRequest {

    @JsonProperty("candidate_profile")
    private CandidateProfile candidateProfile;

    @JsonProperty("job_requirement")
    private JobRequirementData jobRequirement;

    @JsonProperty("session_config")
    private SessionConfigData sessionConfig;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionConfigData {
        @JsonProperty("duration_minutes")
        private Integer durationMinutes;

        @JsonProperty("interview_mode")
        private InterviewEnums.InterviewMode interviewMode;

        @JsonProperty("difficulty")
        private InterviewEnums.DifficultyLevel difficulty;

        @JsonProperty("language")
        private InterviewEnums.Language language;

        @JsonProperty("domain")
        private InterviewEnums.JobDomain domain;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobRequirementData {
        @JsonProperty("basic_info")
        private BasicInfo basicInfo;

        @JsonProperty("competencies")
        private Competencies competencies;

        @JsonProperty("responsibilities")
        private List<String> responsibilities;


        // Sub-classes cho JD
        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class BasicInfo {
            @JsonProperty("job_title")
            private String jobTitle;
            @JsonProperty("industry_domain")
            private String industryDomain;
            @JsonProperty("seniority_level")
            private String seniorityLevel;
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Competencies {
            @JsonProperty("hard_skills")
            private List<String> hardSkills;
            @JsonProperty("tools_and_platforms")
            private List<String> toolsAndPlatforms;
            @JsonProperty("soft_skills")
            private List<String> softSkills;
        }
    }
}