package si.endava.banksimulator.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.services.ElementNotFoundException;

@SpringBootTest
@ActiveProfiles(profiles = "withoutEureka")
class BankControllerTest {

    @Autowired private BankController bankController;

    private static final BankDTO bankDTO =
            new BankDTO(
                    UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    "Sparkasse",
                    "KSPKSI22XXX",
                    "SI",
                    "EUR",
                    true);

    private final List<UUID> uuidsToDelete = new ArrayList<>();

    @AfterEach
    void clearInsertedEntities() {
        if (!uuidsToDelete.isEmpty()) {
            for (UUID uuid : uuidsToDelete) {
                bankController.deleteBank(uuid);
            }
            uuidsToDelete.clear();
        }
    }

    ResponseEntity<Bank> addNewTestBankToDatabase() {
        ResponseEntity<Bank> bank = bankController.addNewBank(bankDTO);
        uuidsToDelete.add(bank.getBody().getUuid());

        return bank;
    }

    @Test
    void ensureThatGetAllBanksWorks() {
        int size = bankController.getAllBanks(null).size();
        addNewTestBankToDatabase();

        int sizeAddedBank = bankController.getAllBanks(null).size();

        assertEquals(size + 1, sizeAddedBank);
    }

    @Test
    void ensureThatGetBankByNameWorks() {
        String name = "Nova";

        List<BankDTO> returnedBanks = bankController.getAllBanks(name);

        assertTrue(returnedBanks.get(0).getBankName().contains(name));
    }

    @Test
    void ensureGetBankByNameThrowsErrorResponseWhenNameNotFound() {
        String name = "123abc";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class, () -> bankController.getAllBanks(name));

        assertTrue(thrown.getMessage().contains("Bank with name " + name + " not found."));
    }

    @Test
    void ensureThatGetBankByUuidWorks() {
        ResponseEntity<Bank> newBank = addNewTestBankToDatabase();

        BankDTO returnedBank = bankController.getByUuid(newBank.getBody().getUuid());

        assertEquals(returnedBank.getBic(), newBank.getBody().getBic());
    }

    @Test
    void ensureGetBankByUuidThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(ElementNotFoundException.class, () -> bankController.getByUuid(uuid));

        assertTrue(thrown.getMessage().contains("Bank with uuid " + uuid + " not exists."));
    }

    @Test
    void ensureThatAddNewBankSavesBankInRepository() {
        ResponseEntity<Bank> newBank = addNewTestBankToDatabase();

        assertEquals(newBank.getBody().getBic(), bankDTO.getBic());
    }

    @Test
    void ensureAddNewBankThrowsErrorResponseWhenBicAlreadyExistsInRepository() {
        final BankDTO bankDTOInvalid =
                new BankDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "Sparkasse",
                        "LJBASI2XXX",
                        "SI",
                        "EUR",
                        true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> bankController.addNewBank(bankDTOInvalid));

        assertEquals(HttpStatus.CONFLICT, thrown.getStatus());
        assertTrue(
                thrown.getMessage()
                        .contains("Bank with bic " + bankDTOInvalid.getBic() + " already exists"));
    }

    @Test
    void ensureAddNewBankThrowsErrorResponseWhenBicIsInvalid() {
        final BankDTO bankDTOInvalid =
                new BankDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "Sparkasse",
                        "LJBASI22XXXXX",
                        "SI",
                        "EUR",
                        true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> bankController.addNewBank(bankDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid bic code"));
    }

    @Test
    void ensureAddNewBankThrowsErrorResponseWhenNameIsInvalid() {
        final BankDTO bankDTOInvalid =
                new BankDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "",
                        "LJBASI2XXX",
                        "SI",
                        "EUR",
                        true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> bankController.addNewBank(bankDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid name"));
    }

    @Test
    void ensureAddNewBankThrowsErrorResponseWhenCountryNameIsInvalid() {
        final BankDTO bankDTOInvalid =
                new BankDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "Sparkasse",
                        "LJBASI2XXX",
                        "",
                        "EUR",
                        true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> bankController.addNewBank(bankDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid country name"));
    }

    @Test
    void ensureAddNewBankThrowsErrorResponseWhenCurrencyCodeIsInvalid() {
        final BankDTO bankDTOInvalid =
                new BankDTO(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        "Sparkasse",
                        "LJBASI2XXX",
                        "SI",
                        "EURO",
                        true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> bankController.addNewBank(bankDTOInvalid));

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Invalid currency code"));
    }

    @Test
    void ensureThatDeleteBankDeletesBankFromRepository() {
        ResponseEntity<Bank> bank = this.bankController.addNewBank(bankDTO);

        bankController.deleteBank(bank.getBody().getUuid());

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> bankController.getByUuid(bank.getBody().getUuid()));

        assertTrue(
                thrown.getMessage()
                        .contains("Bank with uuid " + bank.getBody().getUuid() + " not exists."));
    }

    @Test
    void ensureDeleteBankThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(ElementNotFoundException.class, () -> bankController.deleteBank(uuid));

        assertTrue(thrown.getMessage().contains("Bank with uuid " + uuid + " not exists."));
    }

    @Test
    void ensureThatUpdateBankUpdatesBankInRepository() {
        ResponseEntity<Bank> newBank = addNewTestBankToDatabase();
        BankDTO newBankDTO = bankDTO;
        newBankDTO.setBic("INSBMK22XXX");

        bankController.updateBank(newBank.getBody().getUuid(), newBankDTO);

        assertNotEquals(
                bankController.getByUuid(newBank.getBody().getUuid()).getBic(),
                newBank.getBody().getBic());
        assertEquals(
                bankController.getByUuid(newBank.getBody().getUuid()).getBic(),
                newBankDTO.getBic());
    }

    @Test
    void ensureUpdateBankThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> bankController.updateBank(uuid, bankDTO));

        assertTrue(thrown.getMessage().contains("Bank with uuid " + uuid + " not exists."));
    }
}
