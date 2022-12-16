package si.endava.banksimulator.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.net.URI;
import java.util.Arrays;
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
import si.endava.banksimulator.dtos.CustomerDTO;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.services.CustomerService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Returns a list of all customers or query by email")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Customer entities found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            CustomerDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Customer entity not found",
                        content = @Content)
            })
    @GetMapping
    public List<CustomerDTO> getAllCustomers(
            @RequestParam(name = "customerEmail", required = false) String email) {
        return email == null
                ? customerService.getAllCustomers()
                : Arrays.asList(customerService.findByEmailDTO(email));
    }

    @Operation(summary = "Returns an entity of customer")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Customer entity found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            CustomerDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Customer entity not found",
                        content = @Content)
            })
    @GetMapping("/{customerUuid}")
    public CustomerDTO getByUuid(@PathVariable("customerUuid") UUID uuid) {
        return customerService.findByUuidDTO(uuid);
    }

    @Operation(summary = "Add a new customer to the database")
    @ApiResponse(
            responseCode = "201",
            description = "Customer is created",
            content = {
                @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array = @ArraySchema(schema = @Schema(implementation = CustomerDTO.class)))
            })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Customer> addNewCustomer(@RequestBody CustomerDTO customer) {
        Customer savedCustomer = customerService.addNewCustomer(customer);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{uuid}")
                        .buildAndExpand(savedCustomer.getUuid())
                        .toUri();
        return ResponseEntity.created(location).body(savedCustomer);
    }

    @Operation(summary = "Deletes a customer from the database")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Customer is deleted"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Customer entity not found",
                        content = @Content)
            })
    @DeleteMapping(path = "/{customerUuid}")
    public ResponseEntity<Object> deleteCustomer(@PathVariable("customerUuid") UUID uuid) {
        customerService.deleteCustomer(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a customer by its uuid")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Customer was updated",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            CustomerDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Customer not found",
                        content = @Content)
            })
    @PutMapping(path = "/{customerUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CustomerDTO updateCustomer(
            @PathVariable("customerUuid") UUID uuid, @RequestBody CustomerDTO newCustomerEntity) {
        return customerService.updateCustomer(uuid, newCustomerEntity);
    }
}
