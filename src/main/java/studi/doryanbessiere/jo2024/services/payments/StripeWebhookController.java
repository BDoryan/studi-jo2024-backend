package studi.doryanbessiere.jo2024.services.payments;

import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studi.doryanbessiere.jo2024.common.Routes;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(Routes.Stripe.BASE)
@RequiredArgsConstructor
public class StripeWebhookController {

    private final TransactionRepository transactionRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping(Routes.Stripe.WEBHOOK)
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            String type = event.getType();
            log.info("Received Stripe event: {}", type);

            switch (type) {
                // Succès de paiement
                case "checkout.session.completed" -> {
                    Session session = deserialize(event, Session.class);
                    if (session == null) break;
                    onSessionMarked(session, Transaction.TransactionStatus.PAID,
                            "The transaction {} has been marked as PAID.");
                }

                // Échec (paiement async échoué)
                case "checkout.session.async_payment_failed" -> {
                    Session session = deserialize(event, Session.class);
                    if (session == null) break;
                    onSessionMarked(session, Transaction.TransactionStatus.FAILED,
                            "The transaction {} async payment failed and marked as FAILED.");
                }

                // Session expirée (pas de paiement)
                case "checkout.session.expired" -> {
                    Session session = deserialize(event, Session.class);
                    if (session == null) break;
                    onSessionMarked(session, Transaction.TransactionStatus.FAILED,
                            "The transaction {} has expired and marked as FAILED.");
                }

                // (Optionnel) D’autres événements → log uniquement
                case "charge.refunded" -> log.info("Stripe event: charge.refunded (link to a tx if you store charge IDs).");
                default -> log.warn("Unhandled Stripe event type: {}", type);
            }

            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            log.error("Stripe webhook error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: " + e.getMessage());
        }
    }

    /**
     * Désérialise le data.object de l'événement.
     * - 1) essaie via getObject()
     * - 2) fallback via JSON brut si absent (versions d'API / SDK)
     */
    private <T extends StripeObject> T deserialize(Event event, Class<T> clazz) {
        var deser = event.getDataObjectDeserializer();

        Optional<StripeObject> obj = deser.getObject();
        if (obj.isPresent()) {
            try {
                return clazz.cast(obj.get());
            } catch (ClassCastException e) {
                log.error("Stripe object type mismatch. Expected {}, got {}",
                        clazz.getSimpleName(), obj.get().getClass().getSimpleName());
                return null;
            }
        }

        // Fallback JSON brut
        String raw = deser.getRawJson();
        if (raw == null || raw.isBlank()) {
            log.warn("Stripe deserialization: raw JSON is empty for event {}", event.getId());
            return null;
        }

        try {
            return ApiResource.GSON.fromJson(raw, clazz);
        } catch (Exception e) {
            log.error("Failed to parse raw JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    private void onSessionMarked(Session session, Transaction.TransactionStatus status, String logMessage) {
        String transactionId = session.getMetadata() != null ? session.getMetadata().get("transaction_id") : null;
        if (transactionId == null) {
            log.warn("No transaction_id in metadata for session {}", session.getId());
            return;
        }
        updateTransactionStatus(transactionId, status, logMessage);
    }

    private void updateTransactionStatus(String transactionId,
                                         Transaction.TransactionStatus status,
                                         String logMessage) {
        try {
            Optional<Transaction> txOpt = transactionRepository.findById(Long.parseLong(transactionId));
            if (txOpt.isEmpty()) {
                log.warn("Transaction {} not found in database.", transactionId);
                return;
            }
            Transaction tx = txOpt.get();
            tx.setStatus(status);
            transactionRepository.save(tx);
            log.info(logMessage, transactionId);
        } catch (NumberFormatException e) {
            log.error("Invalid transaction ID format: {}", transactionId);
        } catch (Exception e) {
            log.error("Error updating transaction {}: {}", transactionId, e.getMessage(), e);
        }
    }
}
