package si.endava.banksimulator.services;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import si.endava.banksimulator.dtos.ContactDTO;
import si.endava.banksimulator.entities.Contact;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.mappers.ContactMapper;
import si.endava.banksimulator.repositories.ContactRepository;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    private final CustomerService customerService;

    public List<ContactDTO> getAllContacts() {
        return contactRepository.findAll().stream()
                .map(contactMapper::contactToContactDTO)
                .collect(Collectors.toList());
    }

    public List<ContactDTO> getContactByNameContaining(String name) {
        return Optional.of(
                        contactRepository.findByNameContaining(name).stream()
                                .map(contactMapper::contactToContactDTO)
                                .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .orElseThrow(
                        () ->
                                new ElementNotFoundException(
                                        "Contact with name " + name + " not found."));
    }

    public ContactDTO findByUuid(UUID uuid) {
        return contactMapper.contactToContactDTO(
                contactRepository
                        .findContactByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Contact with uuid " + uuid + " not exists.")));
    }

    public Contact findByCustomerAndEmail(Customer customer, String email) {
        return contactRepository
                .findByCustomerUuidAndEmail(customer.getUuid(), email)
                .orElseThrow(
                        () ->
                                new ElementNotFoundException(
                                        "Contact with email " + email + " not found."));
    }

    public Contact addNewContact(ContactDTO contact) {

        validate(contact);
        Contact newContact = contactMapper.contactDTOToContact(contact);
        newContact.setCustomer(customerService.findByEmail(contact.getCustomer().getEmail()));
        newContact.setUuid(UUID.randomUUID());
        contactRepository.save(newContact);

        return newContact;
    }

    public void deleteContact(UUID uuid) {
        Contact contact =
                contactRepository
                        .findContactByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Contact with uuid " + uuid + " not exists."));
        contactRepository.deleteById(contact.getId());
    }

    @Transactional
    public ContactDTO updateContact(UUID uuid, ContactDTO newContactListEntity) {
        validate(newContactListEntity);
        Contact existingContact =
                contactRepository
                        .findContactByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Contact with uuid " + uuid + " not exists."));

        contactMapper.mapToContact(newContactListEntity, existingContact);

        contactRepository.save(existingContact);

        return contactMapper.contactToContactDTO(existingContact);
    }

    public void validate(ContactDTO contactDTO) {
        if (StringUtils.isBlank(contactDTO.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name");
        if (StringUtils.isBlank(contactDTO.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email");
    }
}
