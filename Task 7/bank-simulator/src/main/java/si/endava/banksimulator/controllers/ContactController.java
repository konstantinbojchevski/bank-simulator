package si.endava.banksimulator.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import si.endava.banksimulator.dtos.ContactDTO;
import si.endava.banksimulator.entities.Contact;
import si.endava.banksimulator.services.ContactService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/contacts")
public class ContactController {

    private final ContactService contactService;

    @Operation(summary = "Returns a list of all contacts or query by name")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Contact entities found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            ContactDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Contact entity not found",
                        content = @Content)
            })
    @GetMapping
    public List<ContactDTO> getAllContacts(
            @RequestParam(name = "contactName", required = false) String name) {
        return name == null
                ? contactService.getAllContacts()
                : contactService.getContactByNameContaining(name);
    }

    @Operation(summary = "Returns an entity of contact")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Contact entity found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            ContactDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Contact entity not found",
                        content = @Content)
            })
    @GetMapping("/{contactUuid}")
    public ContactDTO getByUuid(@PathVariable("contactUuid") UUID uuid) {
        return contactService.findByUuid(uuid);
    }

    @Operation(summary = "Add a new contact to the database")
    @ApiResponse(
            responseCode = "201",
            description = "Contact is created",
            content = {
                @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array = @ArraySchema(schema = @Schema(implementation = ContactDTO.class)))
            })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Contact> addNewContact(@RequestBody ContactDTO contact) {
        Contact savedContact = contactService.addNewContact(contact);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{uuid}")
                        .buildAndExpand(savedContact.getUuid())
                        .toUri();
        return ResponseEntity.created(location).body(savedContact);
    }

    @Operation(summary = "Deletes a contact from the database")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Contact is deleted"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Contact entity not found",
                        content = @Content)
            })
    @DeleteMapping(path = "/{contactUuid}")
    public ResponseEntity<Object> deleteContact(@PathVariable("contactUuid") UUID uuid) {
        contactService.deleteContact(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a contact by its id")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Contact was updated",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            ContactDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Contact not found",
                        content = @Content)
            })
    @PutMapping(path = "/{contactUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ContactDTO updateContact(
            @PathVariable("contactUuid") UUID uuid, @RequestBody ContactDTO newContactListEntity) {
        return contactService.updateContact(uuid, newContactListEntity);
    }
}
