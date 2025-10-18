package studi.doryanbessiere.jo2024.services.admins.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Données d'entrée nécessaires à la connexion d'un administrateur.
 */
@Schema(description = "Requête de connexion administrateur")
public record AdminLoginRequest(

        @Schema(description = "Adresse e-mail de l’administrateur", example = "admin@jo2024.fr")
        @Email @NotBlank String email,

        @Schema(description = "Mot de passe de l’administrateur", example = "MotDePasseSecurise123!")
        @NotBlank String password
) {}
