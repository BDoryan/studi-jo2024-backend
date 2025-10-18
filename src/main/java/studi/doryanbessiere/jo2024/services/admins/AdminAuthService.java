package studi.doryanbessiere.jo2024.services.admins;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import studi.doryanbessiere.jo2024.services.admins.dto.*;
import studi.doryanbessiere.jo2024.shared.JwtService;
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;

/**
 * Service gérant la logique d’authentification des administrateurs.
 */
@Service
@RequiredArgsConstructor
@Hidden // non visible dans Swagger UI
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Authentifie un administrateur à partir de son e-mail et mot de passe.
     *
     * @param request requête contenant les identifiants de connexion
     * @return un objet contenant le jeton JWT et les infos de l’administrateur
     * @throws InvalidCredentialsException si les identifiants sont incorrects
     */
    public AdminAuthResponse login(AdminLoginRequest request) {
        var admin = adminRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(admin.getEmail(), "ADMIN");
        return new AdminAuthResponse(token, admin.getEmail(), admin.getFullName());
    }
}
