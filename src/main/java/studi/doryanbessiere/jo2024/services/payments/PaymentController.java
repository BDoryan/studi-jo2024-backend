package studi.doryanbessiere.jo2024.services.payments;

import com.stripe.model.checkout.Session;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.services.payments.dto.CreateCheckoutRequest;
import studi.doryanbessiere.jo2024.shared.security.CustomerOnly;

import java.util.Map;

@RestController
@RequestMapping(Routes.Payment.BASE)
@RequiredArgsConstructor
@Tag(name = "Paiements", description = "Gestion du processus de paiement Stripe côté client")
public class PaymentController {

    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;

    @Operation(
            summary = "Créer une session de paiement Stripe",
            description = """
                    Ce point d'entrée permet à un client authentifié de créer une session de paiement Stripe pour une offre donnée.
                    Une URL de redirection est renvoyée afin que le client puisse procéder au paiement sur la page Stripe sécurisée.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Session Stripe créée avec succès",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                              "checkout_url": "https://checkout.stripe.com/pay/cs_test_abc123...",
                                              "session_id": "cs_test_abc123..."
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Requête invalide (offre inexistante ou paramètre manquant)"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur Stripe ou application")
            }
    )
    @PostMapping(Routes.Payment.CHECKOUT)
    @CustomerOnly
    public ResponseEntity<?> createCheckout(@Valid @RequestBody CreateCheckoutRequest request,
                                            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            Session session = paymentService.createCheckoutSession(request.getOfferId(), authorizationHeader);
            return ResponseEntity.ok(Map.of(
                    "checkout_url", session.getUrl(),
                    "session_id", session.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Obtenir le statut d'une transaction Stripe",
            description = """
                    Permet de connaître l'état actuel d'une transaction Stripe à partir de son identifiant de session (`session_id`).
                    Ce point d'entrée est utile pour afficher la page de confirmation ou d'échec après le paiement.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statut trouvé avec succès",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                              "session_id": "cs_test_abc123...",
                                              "status": "PAID"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "404", description = "Aucune transaction trouvée pour cet identifiant"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    @GetMapping(Routes.Payment.STATUS)
    public ResponseEntity<?> getTransactionStatus(@PathVariable("session_id") String session_id) {
        return transactionRepository.findByStripeSessionId(session_id)
                .map(transaction -> ResponseEntity.ok(Map.of(
                        "session_id", session_id,
                        "status", transaction.getStatus().name()
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of(
                        "error", "Aucune transaction trouvée pour cet identifiant."
                )));
    }
}
