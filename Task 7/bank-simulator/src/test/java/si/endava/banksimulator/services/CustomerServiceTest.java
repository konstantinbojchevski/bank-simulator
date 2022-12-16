package si.endava.banksimulator.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import si.endava.banksimulator.dtos.BankDTO;
import si.endava.banksimulator.dtos.CustomerDTO;
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.feignClient.PaymentNetworkFeignClientMock;
import si.endava.banksimulator.mappers.CustomerMapper;
import si.endava.banksimulator.repositories.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;

    @Mock private CustomerMapper customerMapper;

    @InjectMocks private CustomerService customerService;

    @Mock private BankService bankService;

    @Mock private PaymentNetworkFeignClientMock paymentNetworkFeignClientMock;

    private static final Customer newCustomer =
            Customer.builder()
                    .name("Jonas")
                    .surname("Kimmich")
                    .email("jonas.kimmich@gmail.com")
                    .balance(new BigDecimal("3000.9"))
                    .suspenseBalance(new BigDecimal("300.00"))
                    .paymentNetwork(true)
                    .bank(new Bank())
                    .build();

    private static final CustomerDTO newCustomerDTO =
            CustomerDTO.builder()
                    .name(newCustomer.getName())
                    .surname(newCustomer.getSurname())
                    .email(newCustomer.getEmail())
                    .balance(newCustomer.getBalance())
                    .suspenseBalance(newCustomer.getSuspenseBalance())
                    .paymentNetwork(newCustomer.getPaymentNetwork())
                    .bank(new BankDTO())
                    .build();

    @Test
    void ensureThatGetAllCustomersWorks() {
        customerService.getAllCustomers();

        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void ensureThatFindCustomerByUuidWorks() {
        UUID uuid = UUID.fromString("d0bd56d8-5ca2-469c-b4f8-9bac5e3d5e1e");
        Customer customer = new Customer();
        Mockito.when(customerRepository.findCustomerByUuid(uuid)).thenReturn(Optional.of(customer));

        customerService.findByUuid(uuid);

        verify(customerRepository, times(1)).findCustomerByUuid(uuid);
    }

    @Test
    void ensureThatFindCustomerByUuidThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            customerService.findByUuid(uuid);
                        });

        assertTrue(
                thrown.getMessage()
                        .contains(String.format("Customer with uuid %s not exists.", uuid)));
    }

    @Test
    void ensureThatFindCustomerByEmailWorks() {
        String email = "john.doe@gmail.com";
        Customer customer = new Customer();
        Mockito.when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        customerService.findByEmail(email);

        verify(customerRepository, times(1)).findByEmail(email);
    }

    @Test
    void ensureThatFindCustomerByEmailThrowsErrorResponseWhenEmailNotExists() {
        String email = "123abc";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            customerService.findByEmail(email);
                        });

        assertTrue(
                thrown.getMessage()
                        .contains(String.format("Customer with email %s not found.", email)));
    }

    @Test
    void ensureThatFindCustomerByEmailDTOThrowsErrorResponseWhenEmailNotExists() {
        String email = "123abc";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            customerService.findByEmailDTO(email);
                        });

        assertTrue(
                thrown.getMessage()
                        .contains(String.format("Customer with email %s not found.", email)));
    }

    @Test
    void ensureThatAddNewCustomerSavesCustomerInRepository() {
        Mockito.when(customerMapper.customerDTOToCustomer(newCustomerDTO)).thenReturn(newCustomer);
        Mockito.when(bankService.findByBic(newCustomerDTO.getBank().getBic()))
                .thenReturn(newCustomer.getBank());

        Customer returnedCustomer = customerService.addNewCustomer(newCustomerDTO);

        assertNotNull(returnedCustomer);
        assertEquals(returnedCustomer.getEmail(), newCustomerDTO.getEmail());
        verify(customerRepository, times(1))
                .save(customerMapper.customerDTOToCustomer(newCustomerDTO));
    }

    @Test
    void ensureAddNewCustomerThrowsErrorResponseWhenEmailAlreadyExists() {
        CustomerDTO addCustomerDTO =
                CustomerDTO.builder()
                        .name("a")
                        .surname("b")
                        .email("janez.novak@gmail.com")
                        .balance(new BigDecimal("20"))
                        .suspenseBalance(new BigDecimal("200"))
                        .paymentNetwork(true)
                        .bank(new BankDTO())
                        .build();
        Mockito.when(customerRepository.existsCustomerByEmail(addCustomerDTO.getEmail()))
                .thenReturn(true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerService.addNewCustomer(addCustomerDTO));

        assertEquals(HttpStatus.CONFLICT, thrown.getStatus());
        assertTrue(
                thrown.getMessage()
                        .contains(
                                "Customer with email "
                                        + addCustomerDTO.getEmail()
                                        + " already exists"));
    }

    @Test
    void ensureThatDeleteCustomerDeletesCustomerFromRepository() {
        UUID uuid = UUID.fromString("d0bd56d8-5ca2-469c-b4f8-9bac5e3d5e1e");
        Customer customer = new Customer();
        Mockito.when(customerRepository.findCustomerByUuid(uuid)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(uuid);

        verify(customerRepository, times(1)).deleteById(customer.getId());
    }

    @Test
    void ensureDeleteCustomerThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            customerService.deleteCustomer(uuid);
                        });

        assertTrue(
                thrown.getMessage().contains("Error: Customer with uuid " + uuid + " not exists."));
    }

    @Test
    void ensureThatUpdateCustomerUpdatesCustomerInRepository() {
        CustomerDTO updateCustomerDTO =
                CustomerDTO.builder()
                        .name("a")
                        .surname("b")
                        .email("kai.havertz@gmail.com")
                        .balance(new BigDecimal("20"))
                        .suspenseBalance(new BigDecimal("200"))
                        .paymentNetwork(true)
                        .bank(new BankDTO())
                        .build();
        Mockito.when(customerMapper.customerDTOToCustomer(updateCustomerDTO))
                .thenReturn(newCustomer);
        Mockito.when(bankService.findByBic(updateCustomerDTO.getBank().getBic()))
                .thenReturn(newCustomer.getBank());

        Customer returnedCustomer = customerService.addNewCustomer(updateCustomerDTO);

        Mockito.when(customerRepository.findCustomerByUuid(returnedCustomer.getUuid()))
                .thenReturn(Optional.of(newCustomer));
        Mockito.when(customerMapper.customerToCustomerDTO(newCustomer))
                .thenReturn(updateCustomerDTO);

        CustomerDTO updatedCustomer =
                customerService.updateCustomer(returnedCustomer.getUuid(), updateCustomerDTO);

        assertNotNull(updatedCustomer);
        assertNotEquals(returnedCustomer.getEmail(), updatedCustomer.getEmail());
        assertEquals(updateCustomerDTO.getEmail(), updatedCustomer.getEmail());
    }

    @Test
    void ensureUpdateCustomerThrowsErrorResponseWhenUuidNotExists() {
        UUID toBeUpdated = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> customerService.updateCustomer(toBeUpdated, newCustomerDTO));

        assertTrue(
                thrown.getMessage()
                        .contains("Error: Customer with uuid " + toBeUpdated + " not exists."));
    }

    @Test
    void ensureUpdateCustomerThrowsErrorResponseWhenEmailAlreadyExists() {
        CustomerDTO updateCustomerDTO =
                CustomerDTO.builder()
                        .name("a")
                        .surname("b")
                        .email("janez.novak@gmail.com")
                        .balance(new BigDecimal("20"))
                        .suspenseBalance(new BigDecimal("200"))
                        .paymentNetwork(true)
                        .bank(new BankDTO())
                        .build();
        Mockito.when(customerMapper.customerDTOToCustomer(updateCustomerDTO))
                .thenReturn(newCustomer);
        Mockito.when(bankService.findByBic(updateCustomerDTO.getBank().getBic()))
                .thenReturn(newCustomer.getBank());

        Customer returnedCustomer = customerService.addNewCustomer(updateCustomerDTO);

        Mockito.when(customerRepository.findCustomerByUuid(returnedCustomer.getUuid()))
                .thenReturn(Optional.of(newCustomer));
        Mockito.when(customerRepository.existsCustomerByEmail(updateCustomerDTO.getEmail()))
                .thenReturn(true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                customerService.updateCustomer(
                                        returnedCustomer.getUuid(), updateCustomerDTO));

        assertEquals(HttpStatus.CONFLICT, thrown.getStatus());
        assertTrue(
                thrown.getMessage()
                        .contains(
                                "Customer with email "
                                        + updateCustomerDTO.getEmail()
                                        + " already exists"));
    }
}
