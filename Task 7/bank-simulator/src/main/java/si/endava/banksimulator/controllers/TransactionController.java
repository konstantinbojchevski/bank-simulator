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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import si.endava.banksimulator.dtos.NewTransactionDTO;
import si.endava.banksimulator.dtos.TransactionDTO;
import si.endava.banksimulator.entities.Transaction;
import si.endava.banksimulator.enums.TransactionStatus;
import si.endava.banksimulator.services.TransactionService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Returns a list of all transactions or query by status")
    @ApiResponse(
            responseCode = "200",
            description = "Transaction entities returned",
            content = {
                @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array =
                                @ArraySchema(
                                        schema = @Schema(implementation = TransactionDTO.class)))
            })
    @GetMapping
    public List<TransactionDTO> getAllTransactions(
            @RequestParam(name = "transactionStatus", required = false)
                    TransactionStatus transactionStatus) {
        return transactionStatus == null
                ? transactionService.getAllTransactions()
                : transactionService.getTransactionByStatus(transactionStatus);
    }

    @Operation(summary = "Returns an entity of transaction")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Transaction entity found",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            TransactionDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Transaction entity not found",
                        content = @Content)
            })
    @GetMapping("/{transactionUuid}")
    public TransactionDTO getByUuid(@PathVariable("transactionUuid") UUID uuid) {
        return transactionService.findTransactionByUuidDTO(uuid);
    }

    @Operation(summary = "Add a new transaction to the database")
    @ApiResponse(
            responseCode = "201",
            description = "Transaction is created",
            content = {
                @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array =
                                @ArraySchema(
                                        schema = @Schema(implementation = TransactionDTO.class)))
            })
    @PostMapping(
            path = "customer/{customerUuid}/transaction",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> addNewTransaction(
            @PathVariable("customerUuid") UUID customerUuid,
            @RequestBody NewTransactionDTO transaction) {

        Transaction savedTransaction =
                transactionService.addNewTransaction(
                        customerUuid, transaction.getEmail(), transaction.getSourceAmount());
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{uuid}")
                        .buildAndExpand(savedTransaction.getUuid())
                        .toUri();
        return ResponseEntity.created(location).body(savedTransaction);
    }

    @Operation(summary = "Update a transaction by its uuid")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Transaction was updated",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            TransactionDTO.class)))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Transaction not found",
                        content = @Content)
            })
    @PutMapping(path = "/{transactionUuid}/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionDTO updateTransactionStatus(
            @PathVariable("transactionUuid") UUID uuid,
            @RequestParam(name = "transactionStatus") TransactionStatus transactionStatus) {
        return transactionService.updateTransactionStatus(uuid, transactionStatus);
    }

    @Operation(summary = "Receives notification from payment network")
    @PutMapping(path = "/{transactionUuid}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void completeTransaction(
            @PathVariable("transactionUuid") UUID uuid,
            @RequestParam(name = "bankBIC") String bankBIC) {
        transactionService.completeTransaction(uuid, bankBIC);
    }

    @Operation(summary = "Finalize completed transaction")
    @PutMapping(path = "/{transactionUuid}/finalizeCompletedTransaction")
    public void finalizeCompletedTransaction(
            @PathVariable("transactionUuid") UUID uuid,
            @RequestParam(name = "bankBIC") String bankBIC) {
        transactionService.finalizeCompletedTransaction(uuid, bankBIC);
    }
}
