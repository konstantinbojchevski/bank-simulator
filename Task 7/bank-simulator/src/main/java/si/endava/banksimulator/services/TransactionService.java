package si.endava.banksimulator.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import si.endava.banksimulator.dtos.TransactionDTO;
import si.endava.banksimulator.entities.Customer;
import si.endava.banksimulator.entities.Transaction;
import si.endava.banksimulator.enums.TransactionStatus;
import si.endava.banksimulator.feignClient.PaymentNetworkFeignClientInterface;
import si.endava.banksimulator.mappers.CustomerMapper;
import si.endava.banksimulator.mappers.TransactionMapper;
import si.endava.banksimulator.repositories.CustomerRepository;
import si.endava.banksimulator.repositories.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    private final CustomerService customerService;

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;
    private final ContactService contactService;

    private final PaymentNetworkFeignClientInterface paymentNetworkFeignClientInterface;

    private final KafkaTemplate<String, String> kafkaTemp;

    Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Transactional
    public Transaction internalTransaction(
            Customer source, String targetEmail, Transaction transaction) {
        Customer target = customerService.findByEmail(targetEmail);
        source.setSuspenseBalance(
                source.getSuspenseBalance().subtract(transaction.getSourceAmount()));
        target.setBalance(target.getBalance().add(transaction.getTargetAmount()));
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);

        customerRepository.save(source);
        customerRepository.save(target);
        transactionRepository.save(transaction);

        return transaction;
    }

    public Transaction externalTransaction(
            Customer source, String targetEmail, Transaction transaction) throws Exception {

        try {
            if (!validateTargetCustomer(targetEmail))
                throw new Exception("Target customer invalid.");
            transaction.setTransactionStatus(TransactionStatus.PENDING);
            String dataToSend =
                    source.getUuid().toString()
                            + " "
                            + targetEmail
                            + " "
                            + transaction.getSourceAmount().toString()
                            + " "
                            + transaction.getUuid().toString();
            kafkaTemp.send("paymentNew", dataToSend);

        } catch (Exception e) {
            logger.error("Error: " + e);
            transaction.setTransactionStatus(TransactionStatus.DECLINED);
            throw new Exception("Error occurred while sending payment to network.", e);
        } finally {
            transactionRepository.save(transaction);
        }

        return transaction;
    }

    @KafkaListener(topics = "completeTransaction", groupId = "group")
    public void completeTransactionFromTopic(String incomingTransactionDetails) {
        String[] split = incomingTransactionDetails.split(" ");
        UUID transactionUuid = UUID.fromString(split[0]);
        String bankBIC = split[1];

        completeTransaction(transactionUuid, bankBIC);
    }

    public void completeTransaction(UUID transactionUuid, String bankBIC) {
        final Transaction toComplete = findTransactionByUuid(transactionUuid);
        final Customer target = customerService.findByEmail(toComplete.getContact().getEmail());
        target.setBalance(target.getBalance().add(toComplete.getTargetAmount()));

        customerRepository.save(target);

        String dataToSend = transactionUuid.toString() + " " + bankBIC;
        kafkaTemp.send("notificationFinalizeCompletedTransaction", dataToSend);
    }

    @KafkaListener(topics = "finalizeCompletedTransactionToBank", groupId = "group")
    public void finalizeCompletedTransactionFromTopicForBank(String incomingTransactionDetails) {
        String[] split = incomingTransactionDetails.split(" ");
        UUID transactionUuid = UUID.fromString(split[0]);
        String bankBIC = split[1];

        finalizeCompletedTransaction(transactionUuid, bankBIC);
    }

    public void finalizeCompletedTransaction(UUID transactionUuid, String bankBIC) {
        final Transaction toFinalize = findTransactionByUuid(transactionUuid);
        final Customer source = toFinalize.getCustomer();
        toFinalize.setTransactionStatus(TransactionStatus.COMPLETED);
        source.setSuspenseBalance(
                source.getSuspenseBalance().subtract(toFinalize.getSourceAmount()));

        transactionRepository.save(toFinalize);
    }

    private Boolean validateTargetCustomer(String targetEmail) {
        if (!paymentNetworkFeignClientInterface.validateCustomer(targetEmail).getValid())
            return false;
        else return true;
    }

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::transactionToTransactionDTO)
                .collect(Collectors.toList());
    }

    public Transaction findTransactionByUuid(UUID uuid) {
        return transactionRepository
                .findTransactionByUuid(uuid)
                .orElseThrow(
                        () ->
                                new ElementNotFoundException(
                                        "Transaction with uuid " + uuid + " not exists."));
    }

    public TransactionDTO findTransactionByUuidDTO(UUID uuid) {
        return transactionMapper.transactionToTransactionDTO(
                transactionRepository
                        .findTransactionByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Transaction with uuid " + uuid + " not exists.")));
    }

    public List<TransactionDTO> getTransactionByStatus(TransactionStatus transactionStatus) {
        return Optional.of(
                        transactionRepository.findByTransactionStatus(transactionStatus).stream()
                                .map(transactionMapper::transactionToTransactionDTO)
                                .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .orElseThrow(
                        () ->
                                new ElementNotFoundException(
                                        "Transaction with status "
                                                + transactionStatus
                                                + " not found."));
    }

    public Transaction addNewTransaction(
            UUID customerUuid, String targetEmail, BigDecimal sourceAmount) {

        Customer source = customerService.findByUuid(customerUuid);

        Transaction newTransaction = new Transaction();

        newTransaction.setCustomer(source);
        newTransaction.setContact(contactService.findByCustomerAndEmail(source, targetEmail));
        newTransaction.setSourceAmount(sourceAmount);
        newTransaction.setSourceCurrency(source.getBank().getCurrency());
        newTransaction.setTargetCurrency(source.getBank().getCurrency());
        newTransaction.setExchangeRate(new BigDecimal("1"));
        newTransaction.setTargetAmount(sourceAmount.multiply(newTransaction.getExchangeRate()));
        newTransaction.setDate(LocalDateTime.now());
        newTransaction.setUuid(UUID.randomUUID());

        if (source.getBalance().compareTo(sourceAmount) < 0) {
            newTransaction.setTransactionStatus(TransactionStatus.DECLINED);
        } else {
            source.setBalance(source.getBalance().subtract(sourceAmount));
            source.setSuspenseBalance(source.getSuspenseBalance().add(sourceAmount));

            if (customerService.checkIfExistsCustomerByEmailAndBank(
                    targetEmail, source.getBank())) {
                internalTransaction(source, targetEmail, newTransaction);
            } else {

                try {
                    externalTransaction(source, targetEmail, newTransaction);
                } catch (Exception e) {
                    source.setSuspenseBalance(source.getSuspenseBalance().subtract(sourceAmount));
                    source.setBalance(source.getBalance().add(sourceAmount));
                }
            }
        }

        return newTransaction;
    }

    @Transactional
    public TransactionDTO updateTransactionStatus(UUID uuid, TransactionStatus transactionStatus) {
        Transaction existingTransaction =
                transactionRepository
                        .findTransactionByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Transaction with uuid " + uuid + " not exists."));

        existingTransaction.setTransactionStatus(transactionStatus);

        transactionRepository.save(existingTransaction);

        return transactionMapper.transactionToTransactionDTO(existingTransaction);
    }
}
