package studi.doryanbessiere.jo2024.services.payments;

import com.stripe.model.checkout.Session;
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
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(Routes.Payment.CHECKOUT)
    @CustomerOnly
    public ResponseEntity<?> createCheckout(@Valid @RequestBody CreateCheckoutRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            Session session = paymentService.createCheckoutSession(request.getOfferId(), authorizationHeader);

            return ResponseEntity.ok(Map.of(
                    "checkoutUrl", session.getUrl(),
                    "sessionId", session.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
