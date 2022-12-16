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
import si.endava.banksimulator.dtos.BankDTO;
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.services.BankService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/banks")
public class BankController {

    private final BankService bankService;

    @Operation(summary = "Returns a list of all banks or query by name")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Bank entities found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            BankDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Bank entity not found",
                        content = @Content)
            })
    @GetMapping
    public List<BankDTO> getAllBanks(
            @RequestParam(name = "bankName", required = false) String name) {
        return name == null ? bankService.getAllBanks() : bankService.getBankByNameContaining(name);
    }

    @Operation(summary = "Returns an entity of bank")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Bank entity found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            BankDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Bank entity not found",
                        content = @Content)
            })
    @GetMapping("/{bankUuid}")
    public BankDTO getByUuid(@PathVariable("bankUuid") UUID uuid) {
        return bankService.findByUuid(uuid);
    }

    @Operation(summary = "Add a new bank to the database")
    @ApiResponse(
            responseCode = "201",
            description = "Bank is created",
            content = {
                @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array = @ArraySchema(schema = @Schema(implementation = BankDTO.class)))
            })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Bank> addNewBank(@RequestBody BankDTO bank) {
        Bank savedBank = bankService.addNewBank(bank);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{uuid}")
                        .buildAndExpand(savedBank.getUuid())
                        .toUri();
        return ResponseEntity.created(location).body(savedBank);
    }

    @Operation(summary = "Deletes a bank from the database")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Bank is deleted"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Bank entity not found",
                        content = @Content)
            })
    @DeleteMapping(path = "/{bankUuid}")
    public ResponseEntity<Object> deleteBank(@PathVariable("bankUuid") UUID uuid) {
        bankService.deleteBank(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a bank by its uuid")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Bank was updated",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            BankDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Bank not found",
                        content = @Content)
            })
    @PutMapping(path = "/{bankUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BankDTO updateBank(
            @PathVariable("bankUuid") UUID uuid, @RequestBody BankDTO newBankEntity) {
        return bankService.updateBank(uuid, newBankEntity);
    }
}
