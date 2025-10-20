package studi.doryanbessiere.jo2024.notifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import studi.doryanbessiere.jo2024.notifications.dto.EmailRequest;
import studi.doryanbessiere.jo2024.rendering.TextTemplateEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailNotificationServiceTest {

    private JavaMailSenderImpl mailSender;
    private TextTemplateEngine templateEngine;
    private EmailNotificationService service;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSenderImpl.class);
        templateEngine = new TextTemplateEngine();
        service = new EmailNotificationService(mailSender, templateEngine);

        createTemplate("test-template", "Bonjour {{firstname}}, votre code est {{code}}.");
    }

    @Test
    void shouldSendEmailUsingTemplateSuccessfully() {
        EmailRequest request = EmailRequest.builder()
                .to("jean.dupont@example.com")
                .subject("Code de vérification")
                .templateName("mails/test-template")
                .variables(Map.of("firstname", "Jean", "code", "123456"))
                .build();

        service.sendNotification(request);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();

        System.out.println("==== DEBUG MAIL ====");
        System.out.println("To: " + sentMessage.getTo()[0]);
        System.out.println("Subject: " + sentMessage.getSubject());
        System.out.println("Body:\n" + sentMessage.getText());
        System.out.println("====================");

        assertEquals("jean.dupont@example.com", sentMessage.getTo()[0]);
        assertEquals("Code de vérification", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("Jean"));
        assertTrue(sentMessage.getText().contains("123456"));
    }

    @Test
    void shouldSendEmailWithoutTemplateSuccessfully() {
        // Arrange
        EmailRequest request = EmailRequest.builder()
                .to("user@example.com")
                .subject("Test brut")
                .message("Ceci est un message texte brut")
                .build();

        // Act
        service.sendNotification(request);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("user@example.com", sentMessage.getTo()[0]);
        assertEquals("Test brut", sentMessage.getSubject());
        assertEquals("Ceci est un message texte brut", sentMessage.getText());
    }

    /**
     * Crée un template de test dans le dossier src/test/resources/templates/mails/
     */
    private void createTemplate(String name, String content) {
        try {
            File dir = new File("src/test/resources/templates/mails");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, name + ".txt");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur création template de test", e);
        }
    }
}
