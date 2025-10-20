package studi.doryanbessiere.jo2024.services.offers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.shared.security.AdminOnly;

import java.util.List;

@RestController
@RequestMapping(Routes.Offer.BASE)
@RequiredArgsConstructor
@Tag(name = "Offres", description = "Gestion du catalogue des offres de billetterie")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    @Operation(
            summary = "Lister les offres disponibles",
            description = "Renvoie l'ensemble des offres actives ou inactives pour l'affichage public."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des offres",
                    content = @Content(schema = @Schema(implementation = Offer.class)))
    })
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consulter une offre",
            description = "Renvoie le détail d'une offre identifiée par son identifiant unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offre trouvée",
                    content = @Content(schema = @Schema(implementation = Offer.class))),
            @ApiResponse(responseCode = "404", description = "Offre introuvable", content = @Content)
    })
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    @PostMapping
    @AdminOnly
    @Operation(
            summary = "Créer une nouvelle offre",
            description = "Crée une offre disponible à la vente. Réservé aux administrateurs.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offre créée",
                    content = @Content(schema = @Schema(implementation = Offer.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<Offer> createOffer(@RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.createOffer(offer));
    }

    @PutMapping("/{id}")
    @AdminOnly
    @Operation(
            summary = "Mettre à jour une offre",
            description = "Met à jour les informations d'une offre existante. Réservé aux administrateurs.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offre mise à jour",
                    content = @Content(schema = @Schema(implementation = Offer.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offre introuvable", content = @Content)
    })
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.updateOffer(id, offer));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    @Operation(
            summary = "Supprimer une offre",
            description = "Supprime une offre de la billetterie. Réservé aux administrateurs.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Offre supprimée"),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offre introuvable", content = @Content)
    })
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
