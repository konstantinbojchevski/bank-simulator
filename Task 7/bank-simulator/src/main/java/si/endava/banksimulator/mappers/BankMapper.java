package si.endava.banksimulator.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import si.endava.banksimulator.dtos.BankDTO;
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkBankDTO;

@Mapper(componentModel = "spring")
public interface BankMapper {

    BankDTO bankToBankDTO(Bank bank);

    Bank bankDTOToBank(BankDTO bankDTO);

    @Mapping(target = "customers", ignore = true)
    void mapToBank(BankDTO bankDTO, @MappingTarget Bank bank);

    void mapToBankDTO(Bank bank, @MappingTarget BankDTO bankDTO);

    PaymentNetworkBankDTO bankToPaymentNetworkBankDTO(Bank bank);
}
