package si.endava.banksimulator.entities;

import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contact_list")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Contact {

    @Id
    @SequenceGenerator(
            name = "CONTACT_LIST_ID_SEQUENCE",
            sequenceName = "CONTACT_LIST_ID_SEQUENCE",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTACT_LIST_ID_SEQUENCE")
    @Column(name = "contact_id", nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private long id;

    @Column(name = "contact_uuid", nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.REMOVE)
    private Set<Transaction> transaction;
}
