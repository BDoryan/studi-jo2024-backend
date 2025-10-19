package studi.doryanbessiere.jo2024.services.admins;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.common.exceptions.UnauthorizedException;
import studi.doryanbessiere.jo2024.services.admins.dto.*;
import studi.doryanbessiere.jo2024.services.customers.Customer;
import studi.doryanbessiere.jo2024.shared.JwtService;
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthResponse login(AdminLoginRequest request) {
        var admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(admin.getEmail(), "ADMIN");
        return new AdminAuthResponse(token, admin.getEmail(), admin.getFullName());
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
