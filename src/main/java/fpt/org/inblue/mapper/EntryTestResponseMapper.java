package fpt.org.inblue.mapper;

import fpt.org.inblue.entrytest.dto.response.EntryTestAttemptResponse;
import fpt.org.inblue.entrytest.dto.response.EntryTestQuestionResponse;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntryTestResponseMapper {
    EntryTestQuestionResponse toQuestionResponse(EntryTestAttempt.QuestionItemSnapshot question);

    List<EntryTestQuestionResponse> toQuestionResponses(List<EntryTestAttempt.QuestionItemSnapshot> questions);

    @Mapping(target = "userId", source = "user.id")
    EntryTestAttemptResponse toAttemptResponse(EntryTestAttempt attempt);
}
