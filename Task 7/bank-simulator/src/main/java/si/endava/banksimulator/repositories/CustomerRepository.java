package si.endava.banksimulator.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.entities.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findCustomerByUuid(UUID uuid);

    Boolean existsCustomerByEmail(String email);

    Optional<Customer> findByEmail(String email);

    Boolean existsCustomerByEmailAndBank(String email, Bank bank);
}
