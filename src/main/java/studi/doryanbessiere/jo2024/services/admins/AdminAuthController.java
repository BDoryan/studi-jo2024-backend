package studi.doryanbessiere.jo2024.services.admins;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminAuthResponse;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminLoginRequest;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminMeResponse;

@RestController
@RequestMapping(Routes.Auth.Admin.BASE)
@RequiredArgsConstructor
@Tag(name = "Authentification administrateur", description = "Gestion de la connexion des administrateurs du site JO 2024")
@Slf4j
public class AdminAuthController {


    private final AdminAuthService service;

    @PostMapping(Routes.Auth.Admin.LOGIN)
    @Operation(
            summary = "Connexion d’un administrateur",
            description = "Authentifie un administrateur ou un agent de contrôle et renvoie le jeton JWT assorti au rôle."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie",
                    content = @Content(schema = @Schema(implementation = AdminAuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides", content = @Content)
    })
    public ResponseEntity<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        log.info("Admin login attempt for email: " + request.getEmail());
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping(Routes.Auth.Admin.ME)
    @Operation(
            summary = "Récupérer le profil administrateur",
            description = "Renvoie les informations de l’administrateur authentifié, incluant son rôle et son nom complet.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil récupéré",
                    content = @Content(schema = @Schema(implementation = AdminMeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Jeton manquant ou invalide", content = @Content)
    })
    public ResponseEntity<AdminMeResponse> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }

        return ResponseEntity.ok(service.getAuthenticatedAdmin(token));
    }
}
