package studi.doryanbessiere.jo2024.notifications;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;
import studi.doryanbessiere.jo2024.notifications.dto.EmailRequest;

@SpringBootTest
@ActiveProfiles("test")
class EmailNotificationIntegrationTest {

    @Autowired
    private EmailNotificationService service;

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Test
    @Disabled("You need to enable this test manually to avoid sending emails during CI/CD")
    void shouldActuallySendRealEmail() {
        System.out.printf(
                "SMTP Config → host=%s, port=%d, username=%s%n, password=%s%n",
                mailSender.getHost(),
                mailSender.getPort(),
                mailSender.getUsername(),
                mailSender.getPassword()
        );

        EmailRequest request = EmailRequest.builder()
                .to("doryanbessiere.pro@gmail.com")
                .subject("Test SMTP - JO2024")
                .message("""
                        Bonjour Doryan,

                        Ceci est un test réel depuis ton projet JO2024.
                        Si tu reçois cet e-mail, ton SMTP OVH est bien configuré
                        """)
                .build();

        service.sendNotification(request);
        System.out.println("Email sent successfully!");
    }
}
