package studi.doryanbessiere.jo2024.services.admins.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données d'entrée nécessaires pour l'enregistrement d'un administrateur.
 */
@Schema(description = "Requête de création d'un compte administrateur")
public record AdminRegisterRequest(

        @Schema(description = "Adresse e-mail de l'administrateur", example = "admin@jo2024.fr")
        @Email @NotBlank String email,

        @Schema(description = "Mot de passe de l'administrateur (8 caractères minimum)", example = "MotDePasseSecurise123!")
        @NotBlank @Size(min = 8) String password,

        @Schema(description = "Nom complet de l'administrateur", example = "Jean Dupont")
        @NotBlank String fullName
) {}
