package studi.doryanbessiere.jo2024.services.admins;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import studi.doryanbessiere.jo2024.services.admins.dto.*;
import studi.doryanbessiere.jo2024.shared.JwtService;
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;

@Service
@RequiredArgsConstructor
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
}
