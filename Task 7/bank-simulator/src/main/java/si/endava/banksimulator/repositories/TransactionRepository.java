package si.endava.banksimulator.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.endava.banksimulator.entities.Transaction;
import si.endava.banksimulator.enums.TransactionStatus;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findTransactionByUuid(UUID uuid);

    List<Transaction> findByTransactionStatus(TransactionStatus transactionStatus);
}
