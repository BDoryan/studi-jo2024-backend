package studi.doryanbessiere.jo2024.services.customers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.common.dto.ApiMessageResponse;
import studi.doryanbessiere.jo2024.services.customers.dto.AuthResponse;
import studi.doryanbessiere.jo2024.services.customers.dto.LoginRequest;
import studi.doryanbessiere.jo2024.services.customers.dto.RegisterRequest;
import studi.doryanbessiere.jo2024.services.tickets.TicketService;
import studi.doryanbessiere.jo2024.services.tickets.dto.TicketResponse;
import studi.doryanbessiere.jo2024.shared.security.CustomerOnly;

import java.util.List;

@RestController
@RequestMapping(Routes.Auth.Customer.BASE)
@RequiredArgsConstructor
@Tag(
        name = "Compte client - Authentification",
        description = """
                Cette catégorie regroupe les opérations liées à l'authentification des clients,
                incluant l'inscription de nouveaux utilisateurs et la connexion des utilisateurs existants.
                """
)
public class CustomerAuthController {

    public static final String CUSTOMER_CREATED = "customer_registered";

    private final CustomerAuthService authService;

    @Operation(
            summary = "Inscription d'un nouveau client",
            description = """
                    Cette opération permet à un nouvel utilisateur de créer un compte client en fournissant les informations requises telles que le prénom, le nom, l'adresse e-mail et le mot de passe.
                    Une fois inscrit, le client pourra utiliser ses identifiants pour se connecter et accéder aux fonctionnalités réservées aux utilisateurs authentifiés.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client enregistré avec succès",
                    content = @Content(schema = @Schema(implementation = ApiMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou manquantes", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content)
    })
    @PostMapping(Routes.Auth.Customer.REGISTER)
    public ResponseEntity<ApiMessageResponse> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok(new ApiMessageResponse("success", CUSTOMER_CREATED));
    }

    @Operation(
            summary = "Connexion d'un client existant",
            description = """
                    Cette opération permet à un client existant de se connecter à son compte en fournissant ses identifiants (adresse e-mail et mot de passe).
                    Si les informations sont correctes, le client recevra un jeton d'authentification (JWT) qui lui permettra d'accéder aux ressources protégées de l'API.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification réussie",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content)
    })

    @PostMapping(Routes.Auth.Customer.LOGIN)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(new AuthResponse(authService.login(req)));
    }

    @Operation(
            summary = "Récupération des informations du client authentifié",
            description = """
                    Cette opération permet de récupérer les informations du client actuellement authentifié en utilisant le jeton d'authentification (JWT) fourni dans l'en-tête de la requête.
                    Le client recevra ses détails personnels, tels que le prénom, le nom et l'adresse e-mail.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informations du client récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = Customer.class))),
            @ApiResponse(responseCode = "401", description = "Jeton d'authentification invalide ou manquant", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content)
    })
    @GetMapping(Routes.Auth.Customer.ME)
    public ResponseEntity<Customer> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }
        Customer customer = authService.getAuthenticatedCustomer(token);
        return ResponseEntity.ok(Customer.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .build());
    }

    private final TicketService ticketService;
    private final CustomerAuthService customerAuthService;

    @GetMapping(Routes.Auth.Customer.ME_TICKETS)
    @CustomerOnly
    @Operation(
            summary = "Lister les billets d'un client",
            description = "Renvoie l'ensemble des billets associés au compte connecté.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des billets",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TicketResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    public ResponseEntity<List<TicketResponse>> meTickets(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.substring(7);
        Customer customer = customerAuthService.getAuthenticatedCustomer(token);
        return ResponseEntity.ok(ticketService.getTicketsForCustomer(customer));
    }
}
