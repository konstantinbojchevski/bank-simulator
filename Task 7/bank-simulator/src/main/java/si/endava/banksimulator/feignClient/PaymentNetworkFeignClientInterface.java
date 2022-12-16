package si.endava.banksimulator.feignClient;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkBankDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkCustomerDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkCustomerValidationDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkPaymentDTO;
import si.endava.banksimulator.feignClient.samples.PaymentRequestDTO;

public interface PaymentNetworkFeignClientInterface {

    ResponseEntity<PaymentNetworkBankDTO> registerNewBank(PaymentNetworkBankDTO bank);

    ResponseEntity<Void> unregisterBank(UUID uuid);

    PaymentNetworkBankDTO updateBank(UUID uuid, PaymentNetworkBankDTO newBankEntity);

    List<PaymentNetworkCustomerDTO> getAllCustomers(String email);

    ResponseEntity<PaymentNetworkCustomerDTO> registerNewCustomer(
            PaymentNetworkCustomerDTO customer);

    ResponseEntity<Void> unregisterCustomer(UUID uuid);

    PaymentNetworkCustomerDTO updateCustomer(
            UUID uuid, PaymentNetworkCustomerDTO newCustomerEntity);

    ResponseEntity<PaymentNetworkPaymentDTO> addNewPayment(
            UUID customerUuid, PaymentRequestDTO paymentRequestDTO);

    void finalizeCompletedTransaction(UUID uuid, String bankBIC);

    PaymentNetworkCustomerValidationDTO validateCustomer(String email);
}
