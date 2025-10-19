package studi.doryanbessiere.jo2024.services.tickets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    boolean existsBySecretKey(String secretKey);
    Optional<Ticket> findByTransactionId(Long transactionId);
    List<Ticket> findAllByCustomerSecretOrderByCreatedAtDesc(String customerSecret);
}
