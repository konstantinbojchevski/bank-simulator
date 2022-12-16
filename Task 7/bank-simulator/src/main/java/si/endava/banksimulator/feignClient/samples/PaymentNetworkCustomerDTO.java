package si.endava.banksimulator.feignClient.samples;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import si.endava.banksimulator.enums.CustomerStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentNetworkCustomerDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    private String email;

    private CustomerStatus customerStatus;

    private String bankBIC;
}
