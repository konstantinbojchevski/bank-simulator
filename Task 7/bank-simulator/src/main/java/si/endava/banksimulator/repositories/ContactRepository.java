package si.endava.banksimulator.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.endava.banksimulator.entities.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findContactByUuid(UUID uuid);

    List<Contact> findByNameContaining(String name);

    Optional<Contact> findByCustomerUuidAndEmail(UUID customerUuid, String email);
}
