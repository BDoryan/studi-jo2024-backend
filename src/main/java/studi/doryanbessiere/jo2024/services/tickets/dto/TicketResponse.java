package studi.doryanbessiere.jo2024.services.tickets.dto;

import lombok.Builder;
import lombok.Value;
import studi.doryanbessiere.jo2024.services.tickets.Ticket;

import java.time.LocalDateTime;

@Value
@Builder
public class TicketResponse {
    Long ticketId;
    String ticketSecret;
    Ticket.Status status;
    int entriesAllowed;
    String offerName;
    double amount;
    String transactionStatus;
    LocalDateTime createdAt;
}
