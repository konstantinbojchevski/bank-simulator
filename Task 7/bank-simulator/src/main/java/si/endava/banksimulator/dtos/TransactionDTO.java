package si.endava.banksimulator.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import si.endava.banksimulator.enums.TransactionStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    private BigDecimal sourceAmount;
    private String sourceCurrency;
    private BigDecimal targetAmount;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private TransactionStatus transactionStatus;
    private LocalDateTime date;
    private CustomerDTO customer;
    private ContactDTO contact;
}
