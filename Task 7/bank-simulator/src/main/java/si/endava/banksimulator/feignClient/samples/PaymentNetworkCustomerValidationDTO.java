package si.endava.banksimulator.feignClient.samples;

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
public class PaymentNetworkCustomerValidationDTO {
    private String email;
    private Boolean valid;
}
