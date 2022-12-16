package si.endava.banksimulator.feignClient.samples;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import si.endava.banksimulator.enums.BankStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentNetworkBankDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    private String bankName;
    private String bic;
    private String currency;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BankStatus bankStatus;
}
