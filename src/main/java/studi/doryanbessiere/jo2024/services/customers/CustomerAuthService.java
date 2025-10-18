package studi.doryanbessiere.jo2024.services.customers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import studi.doryanbessiere.jo2024.common.exceptions.BadRequestException;
import studi.doryanbessiere.jo2024.common.exceptions.UnauthorizedException;
import studi.doryanbessiere.jo2024.notifications.EmailNotificationService;
import studi.doryanbessiere.jo2024.notifications.dto.EmailRequest;
import studi.doryanbessiere.jo2024.services.customers.dto.ForgotPasswordRequest;
import studi.doryanbessiere.jo2024.services.customers.dto.LoginRequest;
import studi.doryanbessiere.jo2024.services.customers.dto.RegisterRequest;
import studi.doryanbessiere.jo2024.services.customers.dto.ResetPasswordRequest;
import studi.doryanbessiere.jo2024.shared.JwtService;

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
    private final EmailNotificationService emailService;

    private final Environment env;

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
     * [DISABLED : MVP2]
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

        String frontendUrl = env.getProperty("APP_FRONTEND_URL", "http://localhost:5173");
        String appName = env.getProperty("APP_NAME", "Billetterie JO 2024");
        String supportEmail = env.getProperty("SUPPORT_EMAIL", "support@localhost");

        // Construct the reset URL
        String resetUrl = String.format("%s/reset-password?token=%s", frontendUrl, token);

        // Prepare variables for the email template
        Map<String, Object> vars = Map.of(
                "name", user.getFirstName(),
                "resetUrl", resetUrl,
                "expirationMinutes", 15,
                "supportEmail",  supportEmail,
                "appName",  appName
        );

        // Send the email using the EmailNotificationService
        emailService.sendNotification(
                EmailRequest.builder()
                        .to(user.getEmail())
                        .subject("Réinitialisation de votre mot de passe")
                        .templateName("forgot-password") // ton template (ex: templates/mails/forgot-password.txt)
                        .variables(vars)
                        .build()
        );
    }


    /**
     * [DISABLED : MVP2]
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
