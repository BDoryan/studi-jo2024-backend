package studi.doryanbessiere.jo2024.services.admins;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import studi.doryanbessiere.jo2024.common.exceptions.UnauthorizedException;
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminAuthResponse;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminLoginRequest;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminMeResponse;
import studi.doryanbessiere.jo2024.shared.JwtService;
import studi.doryanbessiere.jo2024.shared.dto.TwoFactorVerificationRequest;
import studi.doryanbessiere.jo2024.shared.twofactor.TwoFactorAuthService;
import studi.doryanbessiere.jo2024.shared.twofactor.TwoFactorToken;
import studi.doryanbessiere.jo2024.shared.twofactor.TwoFactorTokenType;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorAuthService twoFactorAuthService;

    public AdminAuthResponse login(AdminLoginRequest request) {
        var admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String challengeId = twoFactorAuthService.startChallenge(
                admin.getEmail(),
                admin.getFullName(),
                TwoFactorTokenType.ADMIN
        );

        return new AdminAuthResponse(null, admin.getEmail(), admin.getFullName(), true, challengeId);
    }

    public AdminAuthResponse verifyTwoFactor(TwoFactorVerificationRequest request) {
        TwoFactorToken token = twoFactorAuthService.verifyChallenge(
                request.getChallengeId(),
                request.getCode(),
                TwoFactorTokenType.ADMIN
        );

        Admin admin = adminRepository.findByEmail(token.getEmail())
                .orElseThrow(UnauthorizedException::new);

        String jwt = jwtService.generateToken(admin.getEmail(), "ADMIN");
        return new AdminAuthResponse(jwt, admin.getEmail(), admin.getFullName(), false, null);
    }

    public AdminMeResponse getAuthenticatedAdmin(String token) {
        if (!jwtService.isValid(token)) {
            log.warn("Invalid JWT provided during customer lookup");
            throw new UnauthorizedException();
        }

        String email = jwtService.extractSubject(token);
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(UnauthorizedException::new);

        return new AdminMeResponse(admin.getEmail(), admin.getFullName(), admin.getRole());
    }
}
