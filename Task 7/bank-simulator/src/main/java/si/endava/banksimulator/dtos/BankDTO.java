package si.endava.banksimulator.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class BankDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    private String bankName;
    private String bic;
    private String country;
    private String currency;
    private Boolean paymentNetwork;
}
