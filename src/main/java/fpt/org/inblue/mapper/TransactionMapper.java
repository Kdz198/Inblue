package fpt.org.inblue.mapper;

import fpt.org.inblue.model.Transaction;
import fpt.org.inblue.model.dto.request.TransactionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt",ignore=true)
    @Mapping(target = "transactionCode",ignore = true)
    @Mapping(target = "transactionType" ,ignore = true)
    Transaction toEntity(TransactionRequest dto);
}
