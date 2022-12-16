package si.endava.banksimulator.feignClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkBankDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkCustomerDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkCustomerValidationDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkPaymentDTO;
import si.endava.banksimulator.feignClient.samples.PaymentRequestDTO;
import si.endava.banksimulator.mappers.BankMapper;
import si.endava.banksimulator.mappers.CustomerMapper;
import si.endava.banksimulator.repositories.BankRepository;
import si.endava.banksimulator.repositories.CustomerRepository;

@Profile("withoutEureka")
@Service
@RequiredArgsConstructor
public class PaymentNetworkFeignClientMock implements PaymentNetworkFeignClientInterface {

    private final BankRepository bankRepository;

    private final BankMapper bankMapper;

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    @Override
    public ResponseEntity<PaymentNetworkBankDTO> registerNewBank(PaymentNetworkBankDTO bank) {
        return ResponseEntity.of(Optional.of(bank));
    }

    @Override
    public ResponseEntity<Void> unregisterBank(UUID uuid) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public PaymentNetworkBankDTO updateBank(UUID uuid, PaymentNetworkBankDTO updatedBank) {
        return updatedBank;
    }

    @Override
    public List<PaymentNetworkCustomerDTO> getAllCustomers(String email) {
        return customerRepository.findAll().stream()
                .map(customerMapper::customerToPaymentNetworkCustomerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<PaymentNetworkCustomerDTO> registerNewCustomer(
            PaymentNetworkCustomerDTO customer) {
        return ResponseEntity.of(Optional.of(customer));
    }

    @Override
    public ResponseEntity<Void> unregisterCustomer(UUID uuid) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public PaymentNetworkCustomerDTO updateCustomer(
            UUID uuid, PaymentNetworkCustomerDTO newCustomerEntity) {
        return newCustomerEntity;
    }

    @Override
    public ResponseEntity<PaymentNetworkPaymentDTO> addNewPayment(
            UUID customerUuid, PaymentRequestDTO paymentRequestDTO) {
        return ResponseEntity.of(Optional.of(new PaymentNetworkPaymentDTO()));
    }

    @Override
    public void finalizeCompletedTransaction(UUID uuid, String bankBIC) {}

    @Override
    public PaymentNetworkCustomerValidationDTO validateCustomer(String email) {
        return new PaymentNetworkCustomerValidationDTO();
    }
}
