package studi.doryanbessiere.jo2024.services.payments;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.services.tickets.Ticket;
import studi.doryanbessiere.jo2024.services.tickets.TicketService;

@Slf4j
@RestController
@RequestMapping(Routes.Stripe.BASE)
@RequiredArgsConstructor
@Tag(name = "Paiements Stripe", description = "Gestion des notifications automatiques Stripe (webhooks)")
public class StripeWebhookController {

    private final TransactionRepository transactionRepository;
    private final TicketService ticketService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Operation(
            summary = "Réception des événements Stripe (webhook)",
            description = """
                Cet endpoint est appelé automatiquement par Stripe lorsqu’un paiement est terminé, échoue ou expire.
                Il vérifie la signature du message, extrait l'identifiant de la session et met à jour l'état de la transaction.
                """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Événement reçu et traité avec succès"),
                    @ApiResponse(responseCode = "400", description = "Erreur de validation du webhook Stripe", content = @Content)
            }
    )
    @PostMapping(Routes.Stripe.WEBHOOK)
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            String type = event.getType();
            log.info("Received Stripe event: {}", type);

            String sessionId = extractSessionIdSafely(event);
            if (sessionId == null) {
                log.warn("No session ID found in event payload.");
                return ResponseEntity.ok("No session id found");
            }

            switch (type) {
                case "checkout.session.completed" -> handlePaymentSuccess(sessionId);
                case "checkout.session.async_payment_failed", "payment_intent.payment_failed" -> handlePaymentFailed(sessionId);
                case "checkout.session.expired" -> handlePaymentExpired(sessionId);
                case "checkout.session.async_payment_succeeded" -> handleAsyncPaymentSucceeded(sessionId);
                default -> log.info("Unhandled event type: {}", type);
            }

            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            log.error("Stripe webhook error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Webhook error: " + e.getMessage());
        }
    }

    private void handlePaymentSuccess(String sessionId) {
        updateTransactionStatus(sessionId, Transaction.TransactionStatus.PAID,
                "Payment succeeded for session: {}");
    }

    private void handlePaymentFailed(String sessionId) {
        updateTransactionStatus(sessionId, Transaction.TransactionStatus.FAILED,
                "Payment failed for session: {}");
    }

    private void handlePaymentExpired(String sessionId) {
        updateTransactionStatus(sessionId, Transaction.TransactionStatus.FAILED,
                "Payment expired for session: {}");
    }

    private void handleAsyncPaymentSucceeded(String sessionId) {
        updateTransactionStatus(sessionId, Transaction.TransactionStatus.PAID,
                "Async payment succeeded for session: {}");
    }

    private void updateTransactionStatus(String sessionId, Transaction.TransactionStatus status, String message) {
        transactionRepository.findByStripeSessionId(sessionId).ifPresentOrElse(transaction -> {
            Transaction.TransactionStatus previousStatus = transaction.getStatus();
            transaction.setStatus(status);
            transactionRepository.save(transaction);
            if (status == Transaction.TransactionStatus.PAID && previousStatus != Transaction.TransactionStatus.PAID) {
                Ticket ticket = ticketService.generateTicketForTransaction(transaction.getId());
                log.info("Generated ticket {} for transaction {}", ticket.getId(), transaction.getId());
            }
            log.info(message, sessionId);
        }, () -> log.warn("No transaction found for session ID: {}", sessionId));
    }

    private String extractSessionIdSafely(Event event) {
        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer == null || deserializer.getRawJson() == null) {
                return null;
            }
            String json = deserializer.getRawJson();

            int idx = json.indexOf("\"id\"");
            if (idx == -1) return null;
            int start = json.indexOf('"', idx + 5);
            int end = json.indexOf('"', start + 1);
            if (start == -1 || end == -1) return null;

            return json.substring(start + 1, end);
        } catch (Exception e) {
            log.error("Error extracting session id: {}", e.getMessage());
            return null;
        }
    }
}
