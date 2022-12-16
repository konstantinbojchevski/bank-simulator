package si.endava.banksimulator.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    private String name;
    private String surname;
    private String email;
    private BigDecimal balance;
    private BigDecimal suspenseBalance;
    private boolean paymentNetwork;
    private BankDTO bank;
}
