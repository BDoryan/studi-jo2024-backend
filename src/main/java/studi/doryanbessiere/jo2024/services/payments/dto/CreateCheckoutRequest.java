package studi.doryanbessiere.jo2024.services.payments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Requête pour créer une session de paiement Stripe Checkout")
public class CreateCheckoutRequest {

    @NotNull
    @Schema(description = "Identifiant de l’offre à acheter", example = "1")
    private Long offerId;
}
