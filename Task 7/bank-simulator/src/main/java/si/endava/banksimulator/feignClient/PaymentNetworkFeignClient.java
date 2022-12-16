package si.endava.banksimulator.feignClient;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkBankDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkCustomerDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkCustomerValidationDTO;
import si.endava.banksimulator.feignClient.samples.PaymentNetworkPaymentDTO;
import si.endava.banksimulator.feignClient.samples.PaymentRequestDTO;

@FeignClient("payment-network")
@Profile("withEureka")
public interface PaymentNetworkFeignClient extends PaymentNetworkFeignClientInterface {

    @PostMapping("api/v1/banks")
    ResponseEntity<PaymentNetworkBankDTO> registerNewBank(@RequestBody PaymentNetworkBankDTO bank);

    @DeleteMapping("api/v1/banks/{bankUuid}")
    ResponseEntity<Void> unregisterBank(@PathVariable("bankUuid") UUID uuid);

    @PutMapping("api/v1/banks/{bankUuid}")
    PaymentNetworkBankDTO updateBank(
            @PathVariable("bankUuid") UUID uuid, @RequestBody PaymentNetworkBankDTO newBankEntity);

    @GetMapping("api/v1/customers")
    List<PaymentNetworkCustomerDTO> getAllCustomers(
            @RequestParam(name = "customerEmail", required = false) String email);

    @PostMapping("api/v1/customers")
    ResponseEntity<PaymentNetworkCustomerDTO> registerNewCustomer(
            @RequestBody PaymentNetworkCustomerDTO customer);

    @DeleteMapping(path = "api/v1/customers/{customerUuid}")
    ResponseEntity<Void> unregisterCustomer(@PathVariable("customerUuid") UUID uuid);

    @PutMapping(path = "api/v1/customers/{customerUuid}")
    PaymentNetworkCustomerDTO updateCustomer(
            @PathVariable("customerUuid") UUID uuid,
            @RequestBody PaymentNetworkCustomerDTO newCustomerEntity);

    @PostMapping("api/v1/payments/customer/{customerUuid}/payment")
    ResponseEntity<PaymentNetworkPaymentDTO> addNewPayment(
            @PathVariable("customerUuid") UUID customerUuid,
            @RequestBody PaymentRequestDTO paymentRequestDTO);

    @PutMapping(path = "api/v1/payments/{transactionUuid}/finalizeCompletedTransaction")
    void finalizeCompletedTransaction(
            @PathVariable("transactionUuid") UUID uuid,
            @RequestParam(name = "bankBIC") String bankBIC);

    @GetMapping(path = "api/v1/customers/{customerEmail}/validate")
    PaymentNetworkCustomerValidationDTO validateCustomer(
            @PathVariable("customerEmail") String email);
}
