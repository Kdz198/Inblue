package fpt.org.inblue.mapper;

import fpt.org.inblue.entrytest.dto.response.UserCompetencyResponse;
import fpt.org.inblue.entrytest.model.UserCompetency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserCompetencyMapper {
    @Mapping(target = "userId", source = "user.id")
    UserCompetencyResponse toResponse(UserCompetency competency);
}
