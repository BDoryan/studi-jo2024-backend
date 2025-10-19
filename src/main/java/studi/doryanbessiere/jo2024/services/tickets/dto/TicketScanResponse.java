package studi.doryanbessiere.jo2024.services.tickets.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import studi.doryanbessiere.jo2024.services.tickets.Ticket;

import java.time.LocalDateTime;

@Value
@Builder
public class TicketScanResponse {

    @Schema(description = "Identifiant du ticket")
    Long ticketId;

    @Schema(description = "Statut actuel du ticket")
    Ticket.Status status;

    @Schema(description = "Nombre d’entrées autorisées")
    int entriesAllowed;

    @Schema(description = "Nom de l’offre associée")
    String offerName;

    @Schema(description = "Montant payé pour cette offre")
    double amount;

    @Schema(description = "Date de création du ticket")
    LocalDateTime createdAt;

    @Schema(description = "Informations du client associé au ticket")
    CustomerInfo customer;

    @Value
    @Builder
    public static class CustomerInfo {
        Long id;
        String firstName;
        String lastName;
        String email;
    }
}
