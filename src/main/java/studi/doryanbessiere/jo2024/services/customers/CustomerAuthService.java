package studi.doryanbessiere.jo2024.services.customers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import studi.doryanbessiere.jo2024.common.exceptions.BadRequestException;
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;
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
 * Cette classe gère l'authentification des clients, y compris l'inscription,
 * la connexion, la réinitialisation du mot de passe, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailNotificationService emailService;

    private final Environment env;

    /**
     * Politique de mot de passe :
     * - Au moins 8 caractères
     * - Au moins une lettre majuscule
     * - Au moins une lettre minuscule
     * - Au moins un chiffre
     */
    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    /**
     * Cette méthode enregistre un nouveau client dans le système.
     *
     * @param req la requête d'inscription contenant les informations du client
     */
    public void register(RegisterRequest req) {
        Customer user = Customer.builder()
                .firstName(req.getFirstname())
                .lastName(req.getLastname())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .secretKey(UUID.randomUUID().toString())
                .build();

        customerRepository.save(user);
        log.info("Customer registered with email={}", user.getEmail());
    }

    /**
     * Cette méthode authentifie un client et génère un token JWT.
     *
     * @param req la requête de connexion contenant les identifiants du client
     * @return le token JWT généré
     */
    public String login(LoginRequest req) {
        var userOpt = customerRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            log.warn("Login attempt with unknown email={}", req.getEmail());
            throw new InvalidCredentialsException();
        }

        var user = userOpt.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("Invalid password provided for email={}", req.getEmail());
            throw new InvalidCredentialsException();
        }

        log.info("Customer authenticated email={}", user.getEmail());
        return jwtService.generateToken(user.getEmail(), Map.of("role", "CUSTOMER", "uid", user.getId()));
    }

    /**
     * Cette méthode gère la demande de réinitialisation de mot de passe.
     *
     * @param req la requête de mot de passe oublié contenant l'email du client
     */
    public void forgotPassword(ForgotPasswordRequest req) {
        var user = customerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("Password reset requested for unknown email={}", req.getEmail());
                    return new BadRequestException("user_not_found");
                });

        // TODO: Generate JWT token instead of UUID
        String token = UUID.randomUUID().toString();
        user.setExpireToken(token);
        customerRepository.save(user);
        log.info("Password reset token generated for email={}", user.getEmail());

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
                "supportEmail", supportEmail,
                "appName", appName
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
        log.info("Password reset email enqueued for email={}", user.getEmail());
    }

    /**
     * Cette méthode réinitialise le mot de passe d'un client.
     *
     * @param req la requête de réinitialisation de mot de passe contenant le token et le nouveau mot de passe
     */
    public void resetPassword(ResetPasswordRequest req) {
        var user = customerRepository.findAll().stream()
                .filter(u -> req.getToken().equals(u.getExpireToken()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Password reset attempted with invalid token");
                    return new BadRequestException("Invalid token");
                });

        // Here: check the jwt token expiration if using JWT

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setExpireToken(null);
        customerRepository.save(user);
        log.info("Password successfully reset for email={}", user.getEmail());
    }

    /**
     * Cette méthode récupère le client authentifié à partir du token JWT.
     *
     * @param token le token JWT
     * @return le client authentifié
     */
    public Customer getAuthenticatedCustomer(String token) {
        if (!jwtService.isValid(token)) {
            log.warn("Invalid JWT provided during customer lookup");
            throw new UnauthorizedException();
        }

        String email = jwtService.extractSubject(token);
        return customerRepository.findByEmail(email)
                .map(customer -> {
                    log.debug("Authenticated customer retrieved email={}", email);
                    return Customer.builder()
                            .id(customer.getId())
                            .firstName(customer.getFirstName())
                            .lastName(customer.getLastName())
                            .email(customer.getEmail())
                            .build();
                })
                .orElseThrow(() -> {
                    log.warn("Authenticated customer not found for email={}", email);
                    return new UnauthorizedException();
                });
    }
}
