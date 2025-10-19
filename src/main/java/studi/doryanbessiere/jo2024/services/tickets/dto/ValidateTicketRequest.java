package studi.doryanbessiere.jo2024.services.tickets.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTicketRequest {

    @Schema(description = "Clé secrète du ticket à valider", example = "TCK-2A7F84F3A5E14DB1A")
    @NotBlank(message = "is_required")
    private String ticketSecret;

    @Schema(description = "Identifiant du client ayant présenté le ticket", example = "15")
    private Long customerId;
}
