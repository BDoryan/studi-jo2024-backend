package studi.doryanbessiere.jo2024.services.admins;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;
import studi.doryanbessiere.jo2024.services.admins.dto.*;
import studi.doryanbessiere.jo2024.services.customers.Customer;

import java.util.logging.Logger;

@RestController
@RequestMapping(Routes.Auth.Admin.BASE)
@RequiredArgsConstructor
@Tag(name = "Authentification administrateur", description = "Gestion de la connexion des administrateurs du site JO 2024")
@Slf4j
public class AdminAuthController {


    private final AdminAuthService service;

    @PostMapping(Routes.Auth.Admin.LOGIN)
    public ResponseEntity<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        log.info("Admin login attempt for email: " + request.getEmail());
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping(Routes.Auth.Admin.ME)
    public ResponseEntity<AdminMeResponse> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }

        return ResponseEntity.ok(service.getAuthenticatedAdmin(token));
    }
}
