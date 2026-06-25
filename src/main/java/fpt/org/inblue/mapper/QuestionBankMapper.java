package fpt.org.inblue.mapper;

import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.model.dto.request.CreateQuestionBankRequest;
import fpt.org.inblue.model.dto.request.UpdateQuestionBankRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuestionBankMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "questionCategory", ignore = true) // set thủ công từ questionCategoryId
    QuestionBank toEntity(CreateQuestionBankRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "questionCategory", ignore = true) // set thủ công từ questionCategoryId nếu có
    void updateQuestionBankFromRequest(UpdateQuestionBankRequest request, @MappingTarget QuestionBank questionBank);
}
