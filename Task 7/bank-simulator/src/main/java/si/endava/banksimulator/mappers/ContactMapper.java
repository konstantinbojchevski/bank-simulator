package si.endava.banksimulator.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import si.endava.banksimulator.dtos.ContactDTO;
import si.endava.banksimulator.entities.Contact;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    ContactDTO contactToContactDTO(Contact contact);

    Contact contactDTOToContact(ContactDTO contactDTO);

    @Mapping(target = "transaction", ignore = true)
    void mapToContact(ContactDTO contactDTO, @MappingTarget Contact contact);

    void mapToContactDTO(Contact contact, @MappingTarget ContactDTO contactDTO);
}
