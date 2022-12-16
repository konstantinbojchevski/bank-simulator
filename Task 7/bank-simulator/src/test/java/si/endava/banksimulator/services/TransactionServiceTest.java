package si.endava.banksimulator.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import si.endava.banksimulator.dtos.BankDTO;
import si.endava.banksimulator.dtos.CustomerDTO;
import si.endava.banksimulator.dtos.NewTransactionDTO;
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.entities.Contact;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.entities.Transaction;
import si.endava.banksimulator.enums.TransactionStatus;
import si.endava.banksimulator.feignClient.PaymentNetworkFeignClientMock;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkCustomerDTO;
import si.endava.banksimulator.mappers.CustomerMapper;
import si.endava.banksimulator.mappers.TransactionMapper;
import si.endava.banksimulator.repositories.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;

    @Mock private TransactionMapper transactionMapper;

    @InjectMocks private TransactionService transactionService;

    @Mock private CustomerMapper customerMapper;

    @Mock private CustomerService customerService;

    @Mock private ContactService contactService;

    @Mock private PaymentNetworkFeignClientMock paymentNetworkFeignClientMock;

    private static final Customer newCustomer =
            Customer.builder()
                    .balance(new BigDecimal("3000.9"))
                    .suspenseBalance(new BigDecimal("1000"))
                    .bank(Bank.builder().currency("EUR").build())
                    .build();

    private static final CustomerDTO target =
            CustomerDTO.builder().bank(BankDTO.builder().currency("GBP").build()).build();

    private static final Contact newContact = Contact.builder().build();

    @Test
    void ensureThatGetAllTransactionsWorks() {
        transactionService.getAllTransactions();

        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void ensureThatGetTransactionByStatusWorks() {
        TransactionStatus transactionStatus = TransactionStatus.COMPLETED;
        Mockito.when(transactionRepository.findByTransactionStatus(transactionStatus))
                .thenReturn(Arrays.asList(new Transaction()));

        transactionService.getTransactionByStatus(transactionStatus);

        verify(transactionRepository, times(1)).findByTransactionStatus(transactionStatus);
    }

    @Test
    void ensureGetTransactionByStatusThrowsErrorResponseWhenTransactionWithStatusNotFound() {
        TransactionStatus transactionStatus = TransactionStatus.DECLINED;

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> transactionService.getTransactionByStatus(transactionStatus));

        assertTrue(
                thrown.getMessage()
                        .contains("Transaction with status " + transactionStatus + " not found."));
    }

    @Test
    void ensureThatAddNewInternalTransactionSavesTransactionInRepository() {
        final NewTransactionDTO newTransactionDTO =
                new NewTransactionDTO("max.irving@gmail.com", BigDecimal.valueOf(1000));
        Mockito.when(customerService.findByUuid(newCustomer.getUuid())).thenReturn(newCustomer);
        Mockito.when(customerService.findByEmailDTO(newTransactionDTO.getEmail()))
                .thenReturn(target);
        Mockito.when(customerMapper.customerDTOToCustomer(target)).thenReturn(newCustomer);
        Mockito.when(
                        contactService.findByCustomerAndEmail(
                                newCustomer, newTransactionDTO.getEmail()))
                .thenReturn(newContact);
        Mockito.when(
                        customerService.checkIfExistsCustomerByEmailAndBank(
                                newTransactionDTO.getEmail(), newCustomer.getBank()))
                .thenReturn(true);

        transactionService.addNewTransaction(
                newCustomer.getUuid(),
                newTransactionDTO.getEmail(),
                newTransactionDTO.getSourceAmount());

        ArgumentCaptor<Transaction> transactionArgumentCaptor =
                ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());
    }

    @Test
    void ensureThatAddNewExternalTransactionSavesTransactionInRepository() {
        final NewTransactionDTO newTransactionDTO =
                new NewTransactionDTO("max.irving@gmail.com", BigDecimal.valueOf(1000));
        List<PaymentNetworkCustomerDTO> list = new ArrayList<>();
        final PaymentNetworkCustomerDTO paymentNetworkCustomerDTO =
                PaymentNetworkCustomerDTO.builder().build();
        list.add(paymentNetworkCustomerDTO);
        Mockito.when(customerService.findByUuid(newCustomer.getUuid())).thenReturn(newCustomer);
        Mockito.when(customerService.findByEmail(any())).thenReturn(newCustomer);
        Mockito.when(
                        contactService.findByCustomerAndEmail(
                                newCustomer, newTransactionDTO.getEmail()))
                .thenReturn(newContact);
        Mockito.when(
                        customerService.checkIfExistsCustomerByEmailAndBank(
                                newTransactionDTO.getEmail(), newCustomer.getBank()))
                .thenReturn(false);
        Mockito.when(paymentNetworkFeignClientMock.getAllCustomers(newTransactionDTO.getEmail()))
                .thenReturn(list);

        transactionService.addNewTransaction(
                newCustomer.getUuid(),
                newTransactionDTO.getEmail(),
                newTransactionDTO.getSourceAmount());

        ArgumentCaptor<Transaction> transactionArgumentCaptor =
                ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());
    }

    @Test
    void ensureThatAddNewTransactionSetsTransactionStatusDeclineWhenNotEnoughBalance() {
        final NewTransactionDTO newTransactionDTO =
                new NewTransactionDTO("max.irving@gmail.com", BigDecimal.valueOf(5000));
        Mockito.when(customerService.findByUuid(newCustomer.getUuid())).thenReturn(newCustomer);
        Mockito.when(customerService.findByEmailDTO(newTransactionDTO.getEmail()))
                .thenReturn(target);
        Mockito.when(customerMapper.customerDTOToCustomer(target)).thenReturn(newCustomer);
        Mockito.when(
                        contactService.findByCustomerAndEmail(
                                newCustomer, newTransactionDTO.getEmail()))
                .thenReturn(newContact);
        Mockito.when(
                        customerService.checkIfExistsCustomerByEmailAndBank(
                                newTransactionDTO.getEmail(), newCustomer.getBank()))
                .thenReturn(true);

        Transaction savedTransaction =
                transactionService.addNewTransaction(
                        newCustomer.getUuid(),
                        newTransactionDTO.getEmail(),
                        newTransactionDTO.getSourceAmount());

        ArgumentCaptor<Transaction> transactionArgumentCaptor =
                ArgumentCaptor.forClass(Transaction.class);
        assertEquals(TransactionStatus.DECLINED, savedTransaction.getTransactionStatus());
        verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());
    }

    @Test
    void ensureThatUpdateTransactionWorks() {
        UUID uuid = UUID.fromString("ba639cd9-33f2-4163-94a4-e1bf785e44ec");
        TransactionStatus transactionStatus = TransactionStatus.COMPLETED;
        Transaction transaction = new Transaction();
        Mockito.when(transactionRepository.findTransactionByUuid(uuid))
                .thenReturn(Optional.of(transaction));

        transactionService.updateTransactionStatus(uuid, transactionStatus);

        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void ensureUpdateTransactionThrowsErrorResponseWhenUuidNotExists() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        TransactionStatus transactionStatus = TransactionStatus.COMPLETED;

        ElementNotFoundException thrown =
                assertThrows(
                        ElementNotFoundException.class,
                        () -> transactionService.updateTransactionStatus(uuid, transactionStatus));

        assertTrue(
                thrown.getMessage()
                        .contains("Error: Transaction with uuid " + uuid + " not exists."));
    }
}
