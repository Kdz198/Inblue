package fpt.org.inblue.mapper;

import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.dto.request.CreateJobDescriptionRequest;
import fpt.org.inblue.model.dto.request.UpdateJobDescriptionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface JobDescriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rounds", ignore = true)
    @Mapping(target = "appliedCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    JobDescription toEntity(CreateJobDescriptionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rounds", ignore = true)
    @Mapping(target = "appliedCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateJobDescriptionFromRequest(UpdateJobDescriptionRequest request, @MappingTarget JobDescription jobDescription);
}

