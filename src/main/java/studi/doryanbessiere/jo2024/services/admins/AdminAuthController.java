package studi.doryanbessiere.jo2024.services.admins;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.services.admins.dto.*;

/**
 * Contrôleur gérant l'authentification des administrateurs.
 * <p>
 * Permet à un administrateur existant de se connecter à son espace sécurisé.
 */
@RestController
@RequestMapping(Routes.Auth.Admin.BASE)
@RequiredArgsConstructor
@Tag(name = "Authentification administrateur", description = "Gestion de la connexion des administrateurs du site JO 2024")
public class AdminAuthController {

    private final AdminAuthService service;

    /**
     * Endpoint de connexion administrateur.
     *
     * @param request données d’authentification de l’administrateur (e-mail et mot de passe)
     * @return un JWT et les informations de l’administrateur connecté
     */
    @Operation(
            summary = "Connexion d’un administrateur",
            description = """
                    Permet à un administrateur de se connecter avec son e-mail et mot de passe.
                    En cas de succès, renvoie un jeton JWT à utiliser pour les futures requêtes sécurisées.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    description = "Identifiants de connexion de l’administrateur",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminLoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Exemple de requête",
                                    value = """
                                            {
                                              "email": "admin@jo2024.fr",
                                              "password": "MotDePasseSecurise123!"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Connexion réussie",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AdminAuthResponse.class),
                                    examples = @ExampleObject(
                                            name = "Exemple de réponse",
                                            value = """
                                                    {
                                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "email": "admin@jo2024.fr",
                                                      "fullName": "Jean Dupont"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Identifiants invalides",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "status": "error",
                                                      "code": "invalid_credentials",
                                                      "message": "Adresse e-mail ou mot de passe incorrect"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping(Routes.Auth.Admin.LOGIN)
    public ResponseEntity<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }
}
