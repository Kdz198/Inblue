package fpt.org.inblue.mapper;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.MentorInfo;
import fpt.org.inblue.model.dto.response.MentorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MentorMapper {
    MentorResponse toMentorResponse(Mentor mentor);
    List<MentorResponse> toMentorResponseList(List<Mentor> mentors);
    Mentor toEntity(MentorInfo mentorInfo);
    void updateMentorFromDto(MentorInfo mentorInfo, @MappingTarget Mentor mentor);
}
