package si.endava.banksimulator.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import si.endava.banksimulator.dtos.CustomerDTO;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkCustomerDTO;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDTO customerToCustomerDTO(Customer customer);

    Customer customerDTOToCustomer(CustomerDTO customerDTO);

    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "contact", ignore = true)
    void mapToCustomer(CustomerDTO customerDTO, @MappingTarget Customer customer);

    void mapToCustomerDTO(Customer customer, @MappingTarget CustomerDTO customerDTO);

    @Mapping(target = "bankBIC", source = "bank.bic")
    PaymentNetworkCustomerDTO customerToPaymentNetworkCustomerDTO(Customer customer);
}
