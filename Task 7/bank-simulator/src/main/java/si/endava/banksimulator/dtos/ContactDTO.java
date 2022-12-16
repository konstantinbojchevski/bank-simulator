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
public class ContactDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    private String name;
    private String email;
    private CustomerDTO customer;
}
