package fpt.org.inblue.mapper;

import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.dto.response.JobRecommendationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobRecommendationMapper {
    @Mapping(target = "matchPercent", source = "matchPercent")
    JobRecommendationResponse toResponse(JobDescription jobDescription, Double matchPercent);
}
