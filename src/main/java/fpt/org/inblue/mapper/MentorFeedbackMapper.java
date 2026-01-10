package fpt.org.inblue.mapper;

import fpt.org.inblue.model.MentorFeedback;
import fpt.org.inblue.model.dto.request.CreateMentorFeedbackRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorFeedbackRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MentorFeedbackMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    @Mapping(target = "user", ignore = true)
    MentorFeedback toEntity(CreateMentorFeedbackRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    @Mapping(target = "user", ignore = true)
    void fromUpdateToEntity(UpdateMentorFeedbackRequest dto, @MappingTarget MentorFeedback entity);
}
