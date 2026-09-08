package fpt.org.inblue.mapper;

import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.dto.response.JobRecommendationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobRecommendationMapper {
    JobRecommendationResponse toResponse(JobDescription jobDescription);
}
