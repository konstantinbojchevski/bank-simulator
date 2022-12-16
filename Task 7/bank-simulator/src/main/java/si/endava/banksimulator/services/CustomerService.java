package si.endava.banksimulator.services;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import si.endava.banksimulator.dtos.CustomerDTO;
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.feignClient.PaymentNetworkFeignClientInterface;
import si.endava.banksimulator.mappers.CustomerMapper;
import si.endava.banksimulator.repositories.CustomerRepository;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    private final BankService bankService;

    private final PaymentNetworkFeignClientInterface paymentNetworkFeignClientInterface;

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::customerToCustomerDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO findByUuidDTO(UUID uuid) {
        return customerMapper.customerToCustomerDTO(
                customerRepository
                        .findCustomerByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Customer with uuid " + uuid + " not exists.")));
    }

    public Customer findByUuid(UUID uuid) {
        return customerRepository
                .findCustomerByUuid(uuid)
                .orElseThrow(
                        () ->
                                new ElementNotFoundException(
                                        "Customer with uuid " + uuid + " not exists."));
    }

    public CustomerDTO findByEmailDTO(String email) {
        Customer customer =
                customerRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Customer with email " + email + " not found."));
        return customerMapper.customerToCustomerDTO(customer);
    }

    public Customer findByEmail(String email) {
        Customer customer =
                customerRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Customer with email " + email + " not found."));
        return customer;
    }

    public Boolean checkIfExistsCustomerByEmailAndBank(String email, Bank bank) {
        return customerRepository.existsCustomerByEmailAndBank(email, bank);
    }

    @Transactional
    public Customer addNewCustomer(CustomerDTO customer) {
        validate(customer);
        if (customerRepository.existsCustomerByEmail(customer.getEmail()))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer with email " + customer.getEmail() + " already exists");

        Customer newCustomer = customerMapper.customerDTOToCustomer(customer);
        newCustomer.setBank(bankService.findByBic(customer.getBank().getBic()));
        newCustomer.setUuid(UUID.randomUUID());
        customerRepository.save(newCustomer);
        if (newCustomer.getPaymentNetwork()) {
            paymentNetworkFeignClientInterface.registerNewCustomer(
                    customerMapper.customerToPaymentNetworkCustomerDTO(newCustomer));
        }

        return newCustomer;
    }

    public void deleteCustomer(UUID uuid) {
        Customer customer =
                customerRepository
                        .findCustomerByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Customer with uuid " + uuid + " not exists."));
        paymentNetworkFeignClientInterface.unregisterCustomer(uuid);
        customerRepository.deleteById(customer.getId());
    }

    @Transactional
    public CustomerDTO updateCustomer(UUID uuid, CustomerDTO newCustomerEntity) {
        validate(newCustomerEntity);
        Customer existingCustomer =
                customerRepository
                        .findCustomerByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Customer with uuid " + uuid + " not exists."));

        if (customerRepository.existsCustomerByEmail(newCustomerEntity.getEmail()))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer with email " + newCustomerEntity.getEmail() + " already exists");

        Boolean existingNetwork = existingCustomer.getPaymentNetwork();
        customerMapper.mapToCustomer(newCustomerEntity, existingCustomer);

        customerRepository.save(existingCustomer);
        if (existingNetwork) {
            if (existingCustomer.getPaymentNetwork()) {
                paymentNetworkFeignClientInterface.updateCustomer(
                        uuid, customerMapper.customerToPaymentNetworkCustomerDTO(existingCustomer));
            } else {
                paymentNetworkFeignClientInterface.unregisterCustomer(uuid);
            }
        } else {
            if (existingCustomer.getPaymentNetwork()) {
                paymentNetworkFeignClientInterface.registerNewCustomer(
                        customerMapper.customerToPaymentNetworkCustomerDTO(existingCustomer));
            }
        }

        return customerMapper.customerToCustomerDTO(existingCustomer);
    }

    public void validate(CustomerDTO customerDTO) {
        if (StringUtils.isBlank(customerDTO.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name");
        if (StringUtils.isBlank(customerDTO.getSurname()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid surname");
        if (StringUtils.isBlank(customerDTO.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email");
        if (customerDTO.getBalance() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid balance");
        if (customerDTO.getSuspenseBalance() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid suspense balance");
    }
}
