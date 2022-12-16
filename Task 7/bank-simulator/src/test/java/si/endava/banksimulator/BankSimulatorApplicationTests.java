package si.endava.banksimulator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(profiles = "withoutEureka")
class BankSimulatorApplicationTests {

    @Test
    void contextLoads() {}
}
