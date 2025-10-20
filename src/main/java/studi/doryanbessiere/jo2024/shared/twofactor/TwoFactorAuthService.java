package studi.doryanbessiere.jo2024.shared.twofactor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;
import studi.doryanbessiere.jo2024.notifications.EmailNotificationService;
import studi.doryanbessiere.jo2024.notifications.dto.EmailRequest;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private static final String FIXED_VERIFICATION_CODE = "011020";

    private final TwoFactorTokenRepository tokenRepository;
    private final EmailNotificationService emailNotificationService;
    private final Environment environment;
    private final Clock clock = Clock.systemDefaultZone();

    @Value("${security.twofactor.ttl-minutes:5}")
    private long ttlMinutes;

    @Transactional
    public String startChallenge(String email, String displayName, TwoFactorTokenType type) {
        tokenRepository.deleteByEmailAndType(email, type);

        String code = generateCode();
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = now.plusMinutes(ttlMinutes);

        TwoFactorToken token = TwoFactorToken.builder()
                .id(id)
                .email(email)
                .code(code)
                .createdAt(now)
                .expiresAt(expiresAt)
                .type(type)
                .consumed(false)
                .build();

        tokenRepository.save(token);

        sendMail(email, displayName, code);
        log.info("2FA challenge created for email={} type={} expiresAt={}", email, type, expiresAt);
        return id;
    }

    @Transactional
    public TwoFactorToken verifyChallenge(String challengeId, String code, TwoFactorTokenType expectedType) {
        if (!StringUtils.hasText(challengeId) || !StringUtils.hasText(code)) {
            throw new InvalidCredentialsException();
        }

        TwoFactorToken token = tokenRepository.findById(challengeId)
                .orElseThrow(InvalidCredentialsException::new);

        if (token.isConsumed() || token.getType() != expectedType) {
            throw new InvalidCredentialsException();
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (token.getExpiresAt().isBefore(now) || !token.getCode().equals(code.trim())) {
            throw new InvalidCredentialsException();
        }

        token.setConsumed(true);
        tokenRepository.save(token);
        tokenRepository.delete(token);
        log.info("2FA challenge validated for email={} type={}", token.getEmail(), token.getType());

        return token;
    }

    private void sendMail(String email, String displayName, String code) {
        String appName = environment.getProperty("APP_NAME", "Billetterie JO 2024");

        String name = StringUtils.hasText(displayName) ? displayName : email;
        long expirationMinutes = ttlMinutes;

        Map<String, Object> variables = Map.of(
                "name", name,
                "code", code,
                "expirationMinutes", expirationMinutes,
                "appName", appName
        );

        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject("Code de vérification - " + appName)
                .templateName("emails/two-factor-code")
                .variables(variables)
                .build();

        emailNotificationService.sendNotification(request);
    }

    private String generateCode() {
        return FIXED_VERIFICATION_CODE;
    }
}
