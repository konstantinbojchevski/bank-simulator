package si.endava.banksimulator.feignClient.samples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import si.endava.banksimulator.enums.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentNetworkPaymentDTO {
    private UUID uuid;

    private String receiverEmail;
    private BigDecimal sourceAmount;
    private String sourceCurrency;
    private BigDecimal targetAmount;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private PaymentStatus paymentStatus;
    private LocalDateTime date;
    private PaymentNetworkCustomerDTO customer;
}
