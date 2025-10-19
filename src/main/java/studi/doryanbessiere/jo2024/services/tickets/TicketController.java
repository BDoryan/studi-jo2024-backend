package studi.doryanbessiere.jo2024.services.tickets;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.common.dto.ApiMessageResponse;
import studi.doryanbessiere.jo2024.services.customers.CustomerRepository;
import studi.doryanbessiere.jo2024.services.tickets.dto.*;
import studi.doryanbessiere.jo2024.shared.security.AdminOnly;

@RestController
@RequestMapping(Routes.Tickets.BASE)
@RequiredArgsConstructor
@Tag(name = "Tickets - Scan et validation", description = "Opérations d’inspection et validation de billets par les agents autorisés.")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;

    @PostMapping(Routes.Tickets.SCAN)
    @AdminOnly
    @Operation(
            summary = "Scanner un ticket via QR code",
            description = """
                    Permet à un agent de scanner un ticket (via sa clé secrète) et d’obtenir :
                    - les informations du billet
                    - les détails du client associé
                    
                    ⚠️ Le ticket n’est **pas encore désactivé** à cette étape.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Ticket trouvé",
            content = @Content(schema = @Schema(implementation = TicketScanResponse.class)))
    @ApiResponse(responseCode = "400", description = "Ticket introuvable", content = @Content)
    public ResponseEntity<?> scanTicket(@Valid @RequestBody ScanTicketRequest request) {
        var ticket = ticketRepository.findAll()
                .stream()
                .filter(t -> t.getSecretKey().equals(request.getTicketSecret()))
                .findFirst()
                .orElse(null);

        if (ticket == null)
            return ResponseEntity.badRequest().body(new ApiMessageResponse("error", "Ticket introuvable."));

        var customer = ticket.getTransaction().getCustomer();
        var response = TicketScanResponse.builder()
                .ticketId(ticket.getId())
                .status(ticket.getStatus())
                .entriesAllowed(ticket.getEntriesAllowed())
                .offerName(ticket.getTransaction().getOfferName())
                .amount(ticket.getTransaction().getAmount())
                .createdAt(ticket.getCreatedAt())
                .customer(TicketScanResponse.CustomerInfo.builder()
                        .id(customer.getId())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .email(customer.getEmail())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping(Routes.Tickets.VALIDATE)
    @AdminOnly
    @Operation(
            summary = "Valider et désactiver un ticket après confirmation d’identité",
            description = """
                    Une fois l’identité du détenteur vérifiée, cette route désactive le ticket
                    pour empêcher toute réutilisation ultérieure.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Ticket validé avec succès",
            content = @Content(schema = @Schema(implementation = ApiMessageResponse.class)))
    @ApiResponse(responseCode = "400", description = "Ticket déjà utilisé ou introuvable", content = @Content)
    public ResponseEntity<ApiMessageResponse> validateTicket(@Valid @RequestBody ValidateTicketRequest request) {
        var ticket = ticketRepository.findAll()
                .stream()
                .filter(t -> t.getSecretKey().equals(request.getTicketSecret()))
                .findFirst()
                .orElse(null);

        if (ticket == null)
            return ResponseEntity.badRequest().body(new ApiMessageResponse("error", "Ticket introuvable."));

        if (ticket.getStatus() == Ticket.Status.USED)
            return ResponseEntity.badRequest().body(new ApiMessageResponse("error", "Ticket déjà utilisé."));

        var customer = ticket.getTransaction().getCustomer();
        if (!customer.getId().equals(request.getCustomerId()))
            return ResponseEntity.badRequest().body(new ApiMessageResponse("error", "Le client ne correspond pas au détenteur du ticket."));

        ticket.setStatus(Ticket.Status.USED);
        ticketRepository.save(ticket);

        return ResponseEntity.ok(new ApiMessageResponse("success", "Ticket validé et désactivé avec succès."));
    }
}
