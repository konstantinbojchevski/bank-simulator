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
import si.endava.banksimulator.dtos.ContactDTO;
import si.endava.banksimulator.dtos.CustomerDTO;
import si.endava.banksimulator.entities.Contact;
import si.endava.banksimulator.services.ElementNotFoundException;

@SpringBootTest
@ActiveProfiles(profiles = "withoutEureka")
class ContactControllerTest {

    @Autowired private ContactController contactController;

    private static final ContactDTO contactDTO =
            new ContactDTO(
                    UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    "Bobby",
                    "bobby@gmail.com",
                    new CustomerDTO(
                            UUID.fromString("0e02ff72-960d-4615-b460-792f9ba81d17"),
                            "Jonas",
                            "Kimmich",
                            "janez.novak@gmail.com",
                            new BigDecimal("3000.9"),
                            new BigDecimal("300.00"),
                            true,
                            new BankDTO(
                                    UUID.fromString("15de7ff4-f10c-4330-9fc2-7803879566f7"),
                                    "Nova Ljubljanska Banka",
                                    "LJBASI2XXX",
                                    "SI",
                                    "EUR",
                                    true)));

    private final List<UUID> uuidsToDelete = new ArrayList<>();

    @AfterEach
    void clearInsertedEntities() {
        if (!uuidsToDelete.isEmpty()) {
            for (UUID uuid : uuidsToDelete) {
                contactController.deleteContact(uuid);
            }
            uuidsToDelete.clear();
        }
    }

    ResponseEntity<Contact> addNewTestContactToDatabase() {
        ResponseEntity<Contact> contact = contactController.addNewContact(contactDTO);
        uuidsToDelete.add(contact.getBody().getUuid());

        return contact;
    }

    @Test
    void ensureThatGetAllContactsWorks() {
        int size = contactController.getAllContacts(null).size();
        addNewTestContactToDatabase();

        int sizeAddedContact = contactController.getAllContacts(null).size();

        assertEquals(size + 1, sizeAddedContact);
    }

    @Test
    void ensureThatGetAllContactsByNameWorks() {
        String name = "John Doe";

        List<ContactDTO> returnedContacts = contactController.getAllContacts(name);

        assertEquals(name, returnedContacts.get(0).getName());
    }

    @Test
    void ensureGetAllContactsByNameThrowsErrorResponseWhenNameNotFound() {
        String name = "123abc";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> contactController.getAllContacts(name));

        assertTrue(thrown.getMessage().contains("Contact with name " + name + " not found."));
    }

    @Test
    void ensureThatGetContactByUuidWorks() {
        ResponseEntity<Contact> newContact = addNewTestContactToDatabase();

        ContactDTO returnedContact = contactController.getByUuid(newContact.getBody().getUuid());

        assertEquals(returnedContact.getEmail(), newContact.getBody().getEmail());
    }

    @Test
    void ensureGetContactByUuidThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class, () -> contactController.getByUuid(uuid));

        assertTrue(thrown.getMessage().contains("Contact with uuid " + uuid + " not exists."));
    }

    @Test
    void ensureThatAddNewContactSavesContactInRepository() {
        ResponseEntity<Contact> newContact = addNewTestContactToDatabase();

        assertEquals(newContact.getBody().getEmail(), contactDTO.getEmail());
    }

    @Test
    void ensureAddNewContactThrowsErrorResponseWhenNameIsInvalid() {
        final ContactDTO contactDTOInvalid =
                new ContactDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "",
                        "bobby@gmail.com",
                        contactDTO.getCustomer());

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> contactController.addNewContact(contactDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid name"));
    }

    @Test
    void ensureAddNewContactThrowsErrorResponseWhenEmailIsInvalid() {
        final ContactDTO contactDTOInvalid =
                new ContactDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "Bobby",
                        "",
                        contactDTO.getCustomer());

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> contactController.addNewContact(contactDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid email"));
    }

    @Test
    void ensureThatDeleteContactDeletesContactFromRepository() {
        ResponseEntity<Contact> contact = this.contactController.addNewContact(contactDTO);

        contactController.deleteContact(contact.getBody().getUuid());

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> contactController.getByUuid(contact.getBody().getUuid()));

        assertTrue(
                thrown.getMessage()
                        .contains(
                                "Contact with uuid "
                                        + contact.getBody().getUuid()
                                        + " not exists."));
    }

    @Test
    void ensureDeleteContactThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> contactController.deleteContact(uuid));

        assertTrue(thrown.getMessage().contains("Contact with uuid " + uuid + " not exists."));
    }

    @Test
    void ensureThatUpdateContactUpdatesContactInRepository() {
        ResponseEntity<Contact> newContact = addNewTestContactToDatabase();
        ContactDTO newContactDTO = contactDTO;
        newContactDTO.setEmail("kai.havertz@gmail.com");

        contactController.updateContact(newContact.getBody().getUuid(), newContactDTO);

        assertNotEquals(
                contactController.getByUuid(newContact.getBody().getUuid()).getEmail(),
                newContact.getBody().getEmail());
        assertEquals(
                contactController.getByUuid(newContact.getBody().getUuid()).getEmail(),
                newContactDTO.getEmail());
    }

    @Test
    void ensureUpdateContactThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> contactController.updateContact(uuid, contactDTO));

        assertTrue(thrown.getMessage().contains("Contact with uuid " + uuid + " not exists."));
    }
}
