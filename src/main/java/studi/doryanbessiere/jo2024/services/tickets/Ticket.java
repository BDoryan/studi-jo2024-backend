package studi.doryanbessiere.jo2024.services.tickets;

import jakarta.persistence.*;
import lombok.*;
import studi.doryanbessiere.jo2024.services.payments.Transaction;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "secret_key", nullable = false, unique = true, length = 64)
    private String secretKey;

    @Column(name = "customer_secret", nullable = false, length = 64)
    private String customerSecret;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    @ToString.Exclude
    private Transaction transaction;

    @Column(name = "entries_allowed", nullable = false)
    private int entriesAllowed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum Status {
        ACTIVE,
        USED
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.ACTIVE;
        }
    }
}
