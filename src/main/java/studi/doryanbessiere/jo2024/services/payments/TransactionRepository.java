package studi.doryanbessiere.jo2024.services.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByStripeSessionId(String stripeSessionId);
}
