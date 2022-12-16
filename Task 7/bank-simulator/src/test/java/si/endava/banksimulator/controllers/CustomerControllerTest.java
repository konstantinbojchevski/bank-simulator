package si.endava.banksimulator.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import si.endava.banksimulator.dtos.BankDTO;
import si.endava.banksimulator.dtos.CustomerDTO;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.services.ElementNotFoundException;

@SpringBootTest
@ActiveProfiles(profiles = "withoutEureka")
class CustomerControllerTest {

    @Autowired private CustomerController customerController;

    private static final CustomerDTO customerDTO =
            new CustomerDTO(
                    UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    "Jonas",
                    "Kimmich",
                    "jonas.kimmich@gmail.com",
                    new BigDecimal("3000.9"),
                    new BigDecimal("300.00"),
                    true,
                    new BankDTO(
                            UUID.fromString("15de7ff4-f10c-4330-9fc2-7803879566f7"),
                            "Nova Ljubljanska Banka",
                            "LJBASI2XXX",
                            "SI",
                            "EUR",
                            true));

    private final List<UUID> uuidsToDelete = new ArrayList<>();

    @AfterEach
    void clearInsertedEntities() {
        if (!uuidsToDelete.isEmpty()) {
            for (UUID uuid : uuidsToDelete) {
                customerController.deleteCustomer(uuid);
            }
            uuidsToDelete.clear();
        }
    }

    ResponseEntity<Customer> addNewTestCustomerToDatabase() {
        ResponseEntity<Customer> customer = customerController.addNewCustomer(customerDTO);
        uuidsToDelete.add(customer.getBody().getUuid());

        return customer;
    }

    @Test
    void ensureThatGetAllCustomersWorks() {
        int size = customerController.getAllCustomers(null).size();
        addNewTestCustomerToDatabase();

        int sizeAddedCustomer = customerController.getAllCustomers(null).size();

        assertEquals(size + 1, sizeAddedCustomer);
    }

    @Test
    void ensureThatGetCustomerByEmailWorks() {
        String email = "john.doe@gmail.com";

        List<CustomerDTO> returnedCustomer = customerController.getAllCustomers(email);

        assertEquals(email, returnedCustomer.get(0).getEmail());
    }

    @Test
    void ensureGetCustomerByEmailThrowsErrorResponseWhenEmailNotFound() {
        String email = "123abc";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> customerController.getAllCustomers(email));

        assertTrue(thrown.getMessage().contains("Customer with email " + email + " not found."));
    }

    @Test
    void ensureThatGetCustomerByUuidWorks() {
        ResponseEntity<Customer> newCustomer = addNewTestCustomerToDatabase();

        CustomerDTO returnedCustomer =
                customerController.getByUuid(newCustomer.getBody().getUuid());

        assertEquals(returnedCustomer.getEmail(), newCustomer.getBody().getEmail());
    }

    @Test
    void ensureGetCustomerByUuidThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class, () -> customerController.getByUuid(uuid));

        assertTrue(thrown.getMessage().contains("Customer with uuid " + uuid + " not exists."));
    }

    @Test
    void ensureThatAddNewCustomerSavesCustomerInRepository() {
        ResponseEntity<Customer> newCustomer = addNewTestCustomerToDatabase();

        assertEquals(newCustomer.getBody().getEmail(), customerDTO.getEmail());
    }

    @Test
    void ensureAddNewCustomerThrowsErrorResponseWhenEmailAlreadyExistsInRepository() {
        final CustomerDTO customerDTOInvalid =
                new CustomerDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "John",
                        "Doe",
                        "john.doe@gmail.com",
                        new BigDecimal("3000.9"),
                        new BigDecimal("300.00"),
                        true,
                        customerDTO.getBank());

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerController.addNewCustomer(customerDTOInvalid));

        assertEquals(HttpStatus.CONFLICT, thrown.getStatus());
        assertTrue(
                thrown.getMessage()
                        .contains(
                                "Customer with email "
                                        + customerDTOInvalid.getEmail()
                                        + " already exists"));
    }

    @Test
    void ensureAddNewCustomerThrowsErrorResponseWhenEmailIsInvalid() {
        final CustomerDTO customerDTOInvalid =
                new CustomerDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "John",
                        "Doe",
                        "",
                        new BigDecimal("3000.9"),
                        new BigDecimal("300.00"),
                        true,
                        customerDTO.getBank());

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerController.addNewCustomer(customerDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid email"));
    }

    @Test
    void ensureAddNewCustomerThrowsErrorResponseWhenNameIsInvalid() {
        final CustomerDTO customerDTOInvalid =
                new CustomerDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "",
                        "Doe",
                        "john.doe@gmail.com",
                        new BigDecimal("3000.9"),
                        new BigDecimal("300.00"),
                        true,
                        customerDTO.getBank());

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerController.addNewCustomer(customerDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid name"));
    }

    @Test
    void ensureAddNewCustomerThrowsErrorResponseWhenSurnameIsInvalid() {
        final CustomerDTO customerDTOInvalid =
                new CustomerDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "John",
                        "",
                        "john.doe@gmail.com",
                        new BigDecimal("3000.9"),
                        new BigDecimal("300.00"),
                        true,
                        customerDTO.getBank());

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerController.addNewCustomer(customerDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid surname"));
    }

    @Test
    void ensureAddNewCustomerThrowsErrorResponseWhenBalanceIsInvalid() {
        final CustomerDTO customerDTOInvalid =
                new CustomerDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "John",
                        "Doe",
                        "john.doe@gmail.com",
                        null,
                        new BigDecimal("300.00"),
                        true,
                        customerDTO.getBank());

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerController.addNewCustomer(customerDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid balance"));
    }

    @Test
    void ensureAddNewCustomerThrowsErrorResponseWhenSuspenseBalanceIsInvalid() {
        final CustomerDTO customerDTOInvalid =
                new CustomerDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "John",
                        "Doe",
                        "john.doe@gmail.com",
                        new BigDecimal("3000.9"),
                        null,
                        true,
                        customerDTO.getBank());

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerController.addNewCustomer(customerDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid suspense balance"));
    }

    @Test
    void ensureThatDeleteCustomerDeletesCustomerFromRepository() {
        ResponseEntity<Customer> customer = this.customerController.addNewCustomer(customerDTO);

        customerController.deleteCustomer(customer.getBody().getUuid());

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> customerController.getByUuid(customer.getBody().getUuid()));

        assertTrue(
                thrown.getMessage()
                        .contains(
                                "Customer with uuid "
                                        + customer.getBody().getUuid()
                                        + " not exists."));
    }

    @Test
    void ensureDeleteCustomerThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> customerController.deleteCustomer(uuid));

        assertTrue(thrown.getMessage().contains("Customer with uuid " + uuid + " not exists."));
    }

    @Test
    void ensureThatUpdateCustomerUpdatesCustomerInRepository() {
        ResponseEntity<Customer> newCustomer = addNewTestCustomerToDatabase();
        CustomerDTO newCustomerDTO = customerDTO;
        newCustomerDTO.setEmail("kai.havertz@gmail.com");

        customerController.updateCustomer(newCustomer.getBody().getUuid(), newCustomerDTO);

        assertNotEquals(
                customerController.getByUuid(newCustomer.getBody().getUuid()).getEmail(),
                newCustomer.getBody().getEmail());
        assertEquals(
                customerController.getByUuid(newCustomer.getBody().getUuid()).getEmail(),
                newCustomerDTO.getEmail());
    }

    @Test
    void ensureUpdateCustomerThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> customerController.updateCustomer(uuid, customerDTO));

        assertTrue(thrown.getMessage().contains("Customer with uuid " + uuid + " not exists."));
    }
}
