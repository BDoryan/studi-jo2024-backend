package studi.doryanbessiere.jo2024.services.admins.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Réponse renvoyée après une connexion administrateur réussie.
 */
@Schema(description = "Réponse renvoyée après authentification d'un administrateur")
public record AdminAuthResponse(

        @Schema(description = "Jeton JWT pour accéder aux endpoints sécurisés", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Adresse e-mail de l'administrateur", example = "admin@jo2024.fr")
        String email,

        @Schema(description = "Nom complet de l'administrateur", example = "Jean Dupont")
        String fullName
) {}
