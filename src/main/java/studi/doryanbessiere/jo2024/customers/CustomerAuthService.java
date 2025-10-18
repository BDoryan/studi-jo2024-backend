package studi.doryanbessiere.jo2024.customers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import studi.doryanbessiere.jo2024.common.exceptions.BadRequestException;
import studi.doryanbessiere.jo2024.common.exceptions.ConflictException;
import studi.doryanbessiere.jo2024.common.exceptions.UnauthorizedException;
import studi.doryanbessiere.jo2024.shared.JwtService;
import studi.doryanbessiere.jo2024.util.EmailService;
import studi.doryanbessiere.jo2024.customers.dto.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * This class handle the authentication logic for customers
 * - Registration
 * - Login
 * - Forgot Password
 * - Reset Password
 */
@Service
@RequiredArgsConstructor
public class CustomerAuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    /**
     * Password policy : at least 8 characters, one uppercase, one lowercase, one digit
     */
    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    /**
     * This method register a new customer
     *
     * @param req the registration request
     */
    public void register(RegisterRequest req) {
        if (!PASSWORD_POLICY.matcher(req.getPassword()).matches()) {
            throw new BadRequestException("weak_password");
        }

        Customer user = Customer.builder()
                .firstName(req.getFirstname())
                .lastName(req.getLastname())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .secretKey(UUID.randomUUID().toString())
                .build();

        customerRepository.save(user);
    }

    /**
     * This method authenticate a customer and return a JWT token if successful
     * or throw UnauthorizedException if the credentials are invalid
     *
     * @param req the login request
     *
     * @return the JWT token
     */
    public String login(LoginRequest req) {
        var user = customerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("user_not_found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new UnauthorizedException("Invalid credentials");

        return jwtService.generateToken(user.getEmail(), Map.of("role", "CUSTOMER", "uid", user.getId()));
    }

    /**
     * This method handle forgot password requests by generating a reset token
     * and sending a reset link to the user's email.
     *
     * @param req the forgot password request
     */
    public void forgotPassword(ForgotPasswordRequest req) {
        var user = customerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("user_not_found"));

        String token = UUID.randomUUID().toString();
        user.setExpireToken(token);
        user.setExpireTokenAt(LocalDateTime.now().plusMinutes(15));
        customerRepository.save(user);

        String url = "http://localhost:8080/api/auth/customer/reset-password?token=" + token;
        // Here send email to reset password
    }

    /**
     * This method reset the password of a customer using a valid reset token.
     *
     * @param req the reset password request
     */
    public void resetPassword(ResetPasswordRequest req) {
        var user = customerRepository.findAll().stream()
                .filter(u -> req.getToken().equals(u.getExpireToken()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid token"));

        if (user.getExpireTokenAt().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Token expired");

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setExpireToken(null);
        user.setExpireTokenAt(null);
        customerRepository.save(user);
    }
}
