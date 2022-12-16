package si.endava.banksimulator.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import si.endava.banksimulator.enums.TransactionStatus;

@Entity
@Table(name = "transaction")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction {

    @Id
    @SequenceGenerator(
            name = "TRANSACTION_ID_SEQUENCE",
            sequenceName = "TRANSACTION_ID_SEQUENCE",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRANSACTION_ID_SEQUENCE")
    @Column(name = "transaction_id", nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private long id;

    @Column(name = "transaction_uuid", nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @Column(name = "source_amount", nullable = false)
    private BigDecimal sourceAmount;

    @Column(name = "source_currency", nullable = false)
    private String sourceCurrency;

    @Column(name = "target_amount", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "target_currency", nullable = false)
    private String targetCurrency;

    @Column(name = "exchange_rate", nullable = false)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;
}
