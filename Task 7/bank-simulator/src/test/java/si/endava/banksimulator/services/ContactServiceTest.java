package si.endava.banksimulator.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import si.endava.banksimulator.dtos.ContactDTO;
import si.endava.banksimulator.dtos.CustomerDTO;
import si.endava.banksimulator.entities.Contact;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.mappers.ContactMapper;
import si.endava.banksimulator.repositories.ContactRepository;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock private ContactRepository contactRepository;

    @Mock private ContactMapper contactMapper;

    @InjectMocks private ContactService contactService;

    @Mock private CustomerService customerService;

    private static final Contact newContact =
            Contact.builder().name("Bobby").email("bobby@gmail.com").build();

    private static final ContactDTO newContactDTO =
            ContactDTO.builder()
                    .name(newContact.getName())
                    .email(newContact.getEmail())
                    .customer(new CustomerDTO())
                    .build();

    @Test
    void ensureThatGetAllContactsWorks() {
        contactService.getAllContacts();

        verify(contactRepository, times(1)).findAll();
    }

    @Test
    void ensureThatGetContactByNameContainingWorks() {
        String name = "Janez";
        Mockito.when(contactRepository.findByNameContaining(name))
                .thenReturn(Arrays.asList(new Contact()));

        contactService.getContactByNameContaining(name);

        verify(contactRepository, times(1)).findByNameContaining(name);
    }

    @Test
    void ensureGetContactByNameContainingReturnsErrorResponseWhenNameNotExists() {
        String name = "123abc";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            contactService.getContactByNameContaining(name);
                        });

        assertTrue(
                thrown.getMessage()
                        .contains(String.format("Contact with name %s not found.", name)));
    }

    @Test
    void ensureThatFindContactByCustomerAndEmailWorks() {
        String email = "janez.novak@gmail.com";
        Customer customer = new Customer();
        Contact contact = new Contact();
        Mockito.when(contactRepository.findByCustomerUuidAndEmail(customer.getUuid(), email))
                .thenReturn(Optional.of(contact));

        contactService.findByCustomerAndEmail(customer, email);

        verify(contactRepository, times(1)).findByCustomerUuidAndEmail(customer.getUuid(), email);
    }

    @Test
    void ensureFindContactByCustomerAndEmailThrowsErrorResponseWhenEmailNotExists() {
        String email = "123abc";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            contactService.findByCustomerAndEmail(new Customer(), email);
                        });

        assertTrue(
                thrown.getMessage()
                        .contains(String.format("Contact with email %s not found.", email)));
    }

    @Test
    void ensureThatAddNewContactSavesContactInRepository() {
        Mockito.when(contactMapper.contactDTOToContact(newContactDTO)).thenReturn(newContact);
        Mockito.when(customerService.findByEmail(newContactDTO.getCustomer().getEmail()))
                .thenReturn(newContact.getCustomer());

        Contact returnedContact = contactService.addNewContact(newContactDTO);

        assertNotNull(returnedContact);
        assertEquals(returnedContact.getEmail(), newContactDTO.getEmail());
        verify(contactRepository, times(1)).save(contactMapper.contactDTOToContact(newContactDTO));
    }

    @Test
    void ensureThatDeleteContactDeletesContactFromRepository() {
        UUID uuid = UUID.fromString("d79235b3-c24a-440d-a6f7-008e99c2f629");
        Contact contact = new Contact();
        Mockito.when(contactRepository.findContactByUuid(uuid)).thenReturn(Optional.of(contact));

        contactService.deleteContact(uuid);

        verify(contactRepository, times(1)).deleteById(contact.getId());
    }

    @Test
    void ensureDeleteContactThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class, () -> contactService.deleteContact(uuid));

        assertTrue(
                thrown.getMessage()
                        .contains(String.format("Contact with uuid %s not exists.", uuid)));
    }

    @Test
    void ensureThatUpdateContactUpdatesContactInRepository() {
        ContactDTO updateContactDTO = newContactDTO;
        Mockito.when(contactMapper.contactDTOToContact(updateContactDTO)).thenReturn(newContact);
        Mockito.when(customerService.findByEmail(updateContactDTO.getCustomer().getEmail()))
                .thenReturn(newContact.getCustomer());

        Contact returnedContact = contactService.addNewContact(updateContactDTO);

        updateContactDTO.setEmail("kai.havertz@gmail.com");
        Mockito.when(contactRepository.findContactByUuid(returnedContact.getUuid()))
                .thenReturn(Optional.of(newContact));
        Mockito.when(contactMapper.contactToContactDTO(newContact)).thenReturn(updateContactDTO);

        ContactDTO savedContact =
                contactService.updateContact(returnedContact.getUuid(), updateContactDTO);

        assertNotNull(savedContact);
        assertNotEquals(returnedContact.getEmail(), savedContact.getEmail());
        assertEquals(updateContactDTO.getEmail(), savedContact.getEmail());
    }

    @Test
    void ensureUpdateContactThrowsErrorResponseWhenUuidNotExists() {
        UUID toBeUpdated = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> contactService.updateContact(toBeUpdated, newContactDTO));

        assertTrue(
                thrown.getMessage()
                        .contains("Error: Contact with uuid " + toBeUpdated + " not exists."));
    }
}
