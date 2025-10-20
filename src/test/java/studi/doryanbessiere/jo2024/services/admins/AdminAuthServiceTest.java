package studi.doryanbessiere.jo2024.services.admins;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;
import studi.doryanbessiere.jo2024.common.exceptions.UnauthorizedException;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminAuthResponse;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminLoginRequest;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminMeResponse;
import studi.doryanbessiere.jo2024.shared.dto.TwoFactorVerificationRequest;
import studi.doryanbessiere.jo2024.shared.JwtService;
import studi.doryanbessiere.jo2024.shared.twofactor.TwoFactorAuthService;
import studi.doryanbessiere.jo2024.shared.twofactor.TwoFactorToken;
import studi.doryanbessiere.jo2024.shared.twofactor.TwoFactorTokenType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @InjectMocks
    private AdminAuthService adminAuthService;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = Admin.builder()
                .email("admin@jo2024.fr")
                .password("encoded-password")
                .fullName("Admin User")
                .role(Admin.ROLE_ADMIN)
                .build();
    }

    @Test
    void loginShouldReturnChallengeWhenCredentialsAreValid() {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setEmail(admin.getEmail());
        request.setPassword("plain-password");

        // Arrange le dépôt, l'encodeur et le service JWT pour simuler une authentification réussie.
        when(adminRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("plain-password", admin.getPassword())).thenReturn(true);
        when(twoFactorAuthService.startChallenge(admin.getEmail(), admin.getFullName(), TwoFactorTokenType.ADMIN))
                .thenReturn("challenge-id");

        AdminAuthResponse response = adminAuthService.login(request);

        assertEquals(admin.getEmail(), response.email());
        assertEquals(admin.getFullName(), response.fullName());
        assertEquals("challenge-id", response.challengeId());
        assertEquals(true, response.twoFactorRequired());
        verify(twoFactorAuthService).startChallenge(admin.getEmail(), admin.getFullName(), TwoFactorTokenType.ADMIN);
    }

    @Test
    void loginShouldThrowWhenEmailUnknown() {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setEmail("unknown@jo2024.fr");
        request.setPassword("plain");

        when(adminRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> adminAuthService.login(request));
    }

    @Test
    void loginShouldThrowWhenPasswordInvalid() {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setEmail(admin.getEmail());
        request.setPassword("wrong");

        when(adminRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", admin.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> adminAuthService.login(request));
    }

    @Test
    void verifyTwoFactorShouldReturnTokenWhenCodeValid() {
        TwoFactorVerificationRequest request = new TwoFactorVerificationRequest();
        request.setChallengeId("challenge-123");
        request.setCode("123456");

        TwoFactorToken token = TwoFactorToken.builder()
                .email(admin.getEmail())
                .build();

        when(twoFactorAuthService.verifyChallenge("challenge-123", "123456", TwoFactorTokenType.ADMIN))
                .thenReturn(token);
        when(adminRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(jwtService.generateToken(admin.getEmail(), "ADMIN")).thenReturn("jwt-token");

        AdminAuthResponse response = adminAuthService.verifyTwoFactor(request);

        assertEquals("jwt-token", response.token());
        assertEquals(admin.getEmail(), response.email());
        assertEquals(admin.getFullName(), response.fullName());
        assertEquals(false, response.twoFactorRequired());
        assertEquals(null, response.challengeId());
    }

    @Test
    void getAuthenticatedAdminShouldReturnProfileWhenTokenValid() {
        String token = "valid-token";

        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(admin.getEmail());
        when(adminRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        AdminMeResponse response = adminAuthService.getAuthenticatedAdmin(token);

        assertEquals(admin.getEmail(), response.getEmail());
        assertEquals(admin.getFullName(), response.getFullName());
        assertEquals(admin.getRole(), response.getRole());
    }

    @Test
    void getAuthenticatedAdminShouldThrowWhenTokenInvalid() {
        String token = "invalid";
        when(jwtService.isValid(token)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> adminAuthService.getAuthenticatedAdmin(token));
    }

    @Test
    void getAuthenticatedAdminShouldThrowWhenAdminMissing() {
        String token = "valid";

        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(admin.getEmail());
        when(adminRepository.findByEmail(admin.getEmail())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> adminAuthService.getAuthenticatedAdmin(token));
    }
}
