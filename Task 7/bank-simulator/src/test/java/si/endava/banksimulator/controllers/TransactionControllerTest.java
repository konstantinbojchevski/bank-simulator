package si.endava.banksimulator.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import si.endava.banksimulator.dtos.NewTransactionDTO;
import si.endava.banksimulator.dtos.TransactionDTO;
import si.endava.banksimulator.entities.Transaction;
import si.endava.banksimulator.enums.TransactionStatus;
import si.endava.banksimulator.services.ElementNotFoundException;

@SpringBootTest
@ActiveProfiles(profiles = "withoutEureka")
class TransactionControllerTest {

    @Autowired private TransactionController transactionController;

    private static final NewTransactionDTO newTransactionDTO =
            new NewTransactionDTO("max.irving@gmail.com", new BigDecimal("2.0"));

    @Test
    void ensureThatGetAllTransactionsWorks() {
        int size = transactionController.getAllTransactions(null).size();
        transactionController.addNewTransaction(
                UUID.fromString("0e02ff72-960d-4615-b460-792f9ba81d17"), newTransactionDTO);

        int sizeAddedTransaction = transactionController.getAllTransactions(null).size();

        assertEquals(size + 1, sizeAddedTransaction);
    }

    @Test
    void ensureThatGetAllTransactionsByStatusWorks() {
        TransactionStatus transactionStatus = TransactionStatus.PENDING;

        List<TransactionDTO> returnedTransactions =
                transactionController.getAllTransactions(transactionStatus);

        assertEquals(transactionStatus, returnedTransactions.get(0).getTransactionStatus());
    }

    @Test
    void ensureThatGetTransactionByUuidWorks() {
        ResponseEntity<Transaction> transaction =
                transactionController.addNewTransaction(
                        UUID.fromString("0e02ff72-960d-4615-b460-792f9ba81d17"), newTransactionDTO);

        TransactionDTO returnedTransaction =
                transactionController.getByUuid(transaction.getBody().getUuid());

        assertEquals(returnedTransaction.getUuid(), transaction.getBody().getUuid());
    }

    @Test
    void ensureGetTransactionByUuidThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> transactionController.getByUuid(uuid));

        assertTrue(thrown.getMessage().contains("Transaction with uuid " + uuid + " not exists."));
    }

    @Test
    void ensureThatAddNewTransactionSavesTransactionInRepository() {
        ResponseEntity<Transaction> transaction =
                transactionController.addNewTransaction(
                        UUID.fromString("0e02ff72-960d-4615-b460-792f9ba81d17"), newTransactionDTO);

        assertEquals(transaction.getBody().getSourceAmount(), newTransactionDTO.getSourceAmount());
    }

    @Test
    void ensureThatUpdateTransactionStatusUpdatesStatusOfTransactionInRepository() {
        ResponseEntity<Transaction> transaction =
                transactionController.addNewTransaction(
                        UUID.fromString("0e02ff72-960d-4615-b460-792f9ba81d17"), newTransactionDTO);

        TransactionDTO updatedTransaction =
                transactionController.updateTransactionStatus(
                        transaction.getBody().getUuid(), TransactionStatus.PENDING);

        assertNotEquals(
                transaction.getBody().getTransactionStatus(),
                transactionController
                        .getByUuid(transaction.getBody().getUuid())
                        .getTransactionStatus());
        assertEquals(TransactionStatus.PENDING, updatedTransaction.getTransactionStatus());
    }

    @Test
    void ensureUpdateTransactionStatusThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () ->
                                transactionController.updateTransactionStatus(
                                        uuid, TransactionStatus.COMPLETED));

        assertTrue(thrown.getMessage().contains("Transaction with uuid " + uuid + " not exists."));
    }
}
