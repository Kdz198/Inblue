package fpt.org.inblue.mapper;

import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.dto.request.CreateMentorReviewRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorReviewRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MentorReviewMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    @Mapping(target = "user", ignore = true)
    MentorReview toEntity(CreateMentorReviewRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    @Mapping(target = "user", ignore = true)
    void fromUpdateToEntity(UpdateMentorReviewRequest dto, @MappingTarget MentorReview entity);

}
