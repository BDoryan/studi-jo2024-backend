package studi.doryanbessiere.jo2024.services.tickets.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanTicketRequest {

    @Schema(description = "Clé secrète du ticket scannée via le QR Code", example = "TCK-2A7F84F3A5E14DB1A")
    @NotBlank(message = "is_required")
    @JsonProperty("ticket_secret")
    private String ticketSecret;
}
