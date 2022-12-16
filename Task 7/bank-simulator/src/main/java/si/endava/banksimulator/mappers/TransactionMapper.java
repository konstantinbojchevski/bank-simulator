package si.endava.banksimulator.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import si.endava.banksimulator.dtos.TransactionDTO;
import si.endava.banksimulator.entities.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "transactionStatus", source = "transactionStatus")
    TransactionDTO transactionToTransactionDTO(Transaction transaction);

    Transaction transactionDTOToTransaction(TransactionDTO transactionDTO);

    void mapToTransaction(TransactionDTO transactionDTO, @MappingTarget Transaction transaction);

    void mapToTransactionDTO(Transaction transaction, @MappingTarget TransactionDTO transactionDTO);
}
