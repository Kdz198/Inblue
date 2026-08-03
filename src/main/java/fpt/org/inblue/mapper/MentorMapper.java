package fpt.org.inblue.mapper;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.dto.request.CreateMentorRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorRequest;
import fpt.org.inblue.model.dto.response.MentorResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MentorMapper {
    @Mapping(target = "averageRating", expression = "java(roundTwoDecimals(mentor.getAverageRating()))")
    @Mapping(target = "feedbacks", ignore = true)
    MentorResponse toMentorResponse(Mentor mentor);

    List<MentorResponse> toMentorResponseList(List<Mentor> mentors);

    Mentor toEntity(CreateMentorRequest request);

    void updateMentorFromDto(UpdateMentorRequest request, @MappingTarget Mentor mentor);

    default double roundTwoDecimals(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        if (value > 5.0) {
            value = value / 2.0;
        }
        double rounded = Math.round(value * 100.0) / 100.0;
        return Math.min(5.0, Math.max(0.0, rounded));
    }
}
