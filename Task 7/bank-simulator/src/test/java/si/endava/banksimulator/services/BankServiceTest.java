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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import si.endava.banksimulator.dtos.BankDTO;
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.feignClient.PaymentNetworkFeignClientMock;
import si.endava.banksimulator.mappers.BankMapper;
import si.endava.banksimulator.repositories.BankRepository;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock private BankRepository bankRepository;

    @Mock private BankMapper bankMapper;

    @InjectMocks private BankService bankService;

    @Mock private PaymentNetworkFeignClientMock paymentNetworkFeignClientMock;

    private static final Bank newBank =
            Bank.builder()
                    .bankName("Sparkasse")
                    .bic("KSPKSI22XXX")
                    .country("SI")
                    .currency("EUR")
                    .build();

    private static final BankDTO newBankDTO =
            BankDTO.builder()
                    .bankName(newBank.getBankName())
                    .bic(newBank.getBic())
                    .country(newBank.getCountry())
                    .currency(newBank.getCurrency())
                    .build();

    @Test
    void ensureThatGetAllBanksWorks() {
        bankService.getAllBanks();

        verify(bankRepository, times(1)).findAll();
    }

    @Test
    void ensureFindBankByBicWorks() {
        String bic = "LJBASI2XXX";
        Mockito.when(bankRepository.findBankByBic(bic)).thenReturn(Optional.of(new Bank()));

        bankService.findByBic(bic);

        verify(bankRepository, times(1)).findBankByBic(bic);
    }

    @Test
    void ensureFindBankByBicThrowsErrorResponseWhenBicNotExists() {
        String bic = "abc123";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            bankService.findByBic(bic);
                        });

        assertTrue(
                thrown.getMessage().contains(String.format("Bank with bic %s not exists.", bic)));
    }

    @Test
    void ensureThatGetBankByNameContainingWorks() {
        String name = "Nova";
        Mockito.when(bankRepository.findByBankNameContaining(name))
                .thenReturn(Arrays.asList(new Bank()));

        bankService.getBankByNameContaining(name);

        verify(bankRepository, times(1)).findByBankNameContaining(name);
    }

    @Test
    void ensureGetBankByNameContainingThrowsErrorResponseWhenNameNotFound() {
        String name = "abc123";

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            bankService.getBankByNameContaining(name);
                        });

        assertTrue(
                thrown.getMessage().contains(String.format("Bank with name %s not found.", name)));
    }

    @Test
    void ensureThatAddNewBankSavesBankInRepository() {
        Mockito.when(bankMapper.bankDTOToBank(newBankDTO)).thenReturn(newBank);

        Bank returnedBank = bankService.addNewBank(newBankDTO);

        assertNotNull(returnedBank);
        assertEquals(returnedBank.getBankName(), newBankDTO.getBankName());
        verify(bankRepository, times(1)).save(bankMapper.bankDTOToBank(newBankDTO));
    }

    @Test
    void ensureAddNewBankThrowsErrorResponseWhenBicAlreadyExists() {
        BankDTO addBankDTO = newBankDTO;
        addBankDTO.setBic("LJBASI2XXX");
        Mockito.when(bankRepository.existsBankByBic(addBankDTO.getBic())).thenReturn(true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class, () -> bankService.addNewBank(addBankDTO));

        assertEquals(HttpStatus.CONFLICT, thrown.getStatus());
        assertTrue(
                thrown.getMessage()
                        .contains("Bank with bic " + addBankDTO.getBic() + " already exists"));
    }

    @Test
    void ensureThatDeleteBankDeletesBankFromRepository() {
        UUID uuid = UUID.fromString("7c955d37-90b5-41d1-ad1e-49757a0420ac");
        Bank bank = new Bank();
        Mockito.when(bankRepository.findBankByUuid(uuid)).thenReturn(Optional.of(bank));

        bankService.deleteBank(uuid);

        verify(bankRepository, times(1)).deleteById(bank.getId());
    }

    @Test
    void ensureDeleteBankThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> {
                            bankService.deleteBank(uuid);
                        });

        assertTrue(
                thrown.getMessage().contains(String.format("Bank with uuid %s not exists.", uuid)));
    }

    @Test
    void ensureThatUpdateBankUpdatesBankInRepository() {
        BankDTO updateBankDTO = newBankDTO;
        Mockito.when(bankMapper.bankDTOToBank(updateBankDTO)).thenReturn(newBank);

        Bank returnedBank = bankService.addNewBank(updateBankDTO);

        updateBankDTO.setBic("KIDOSI2XXX");
        Mockito.when(bankRepository.findBankByUuid(returnedBank.getUuid()))
                .thenReturn(Optional.of(newBank));
        Mockito.when(bankMapper.bankToBankDTO(newBank)).thenReturn(updateBankDTO);

        BankDTO updatedBank = bankService.updateBank(returnedBank.getUuid(), updateBankDTO);

        assertNotNull(updatedBank);
        assertNotEquals(returnedBank.getBic(), updatedBank.getBic());
        assertEquals(updateBankDTO.getBic(), updatedBank.getBic());
    }

    @Test
    void ensureUpdateBankThrowsErrorResponseWhenUuidNotExists() {
        UUID toBeUpdated = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> bankService.updateBank(toBeUpdated, newBankDTO));

        assertTrue(
                thrown.getMessage()
                        .contains("Error: Bank with uuid " + toBeUpdated + " not exists."));
    }

    @Test
    void ensureUpdateBankThrowsErrorResponseWhenBicAlreadyExists() {
        BankDTO updateBankDTO = newBankDTO;
        Mockito.when(bankMapper.bankDTOToBank(updateBankDTO)).thenReturn(newBank);

        Bank returnedBank = bankService.addNewBank(updateBankDTO);

        updateBankDTO.setBic("LJBASI2XXX");
        Mockito.when(bankRepository.findBankByUuid(returnedBank.getUuid()))
                .thenReturn(Optional.of(newBank));
        Mockito.when(bankRepository.existsBankByBic(updateBankDTO.getBic())).thenReturn(true);

        ResponseStatusException thrown =
                assertThrows(
                        ResponseStatusException.class,
                        () -> bankService.updateBank(returnedBank.getUuid(), updateBankDTO));

        assertEquals(HttpStatus.CONFLICT, thrown.getStatus());
        assertTrue(
                thrown.getMessage()
                        .contains("Bank with bic " + updateBankDTO.getBic() + " already exists"));
    }
}
