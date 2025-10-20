package studi.doryanbessiere.jo2024.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import studi.doryanbessiere.jo2024.services.admins.Admin;
import studi.doryanbessiere.jo2024.services.admins.AdminRepository;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    public void run(String... args) {
        String adminEmail = env.getProperty("ADMIN_DEFAULT_EMAIL", "admin@jo2024.fr");
        String adminPassword = env.getProperty("ADMIN_DEFAULT_PASSWORD");

        if (adminPassword == null || adminPassword.isBlank()) {
            System.err.println("Aucun mot de passe défini pour ADMIN_DEFAULT_PASSWORD - administrateur non créé.");
            return;
        }

        if (!adminRepository.existsByEmail(adminEmail)) {
            Admin admin = Admin.builder()
                    .email(adminEmail)
                    .fullName("Administrateur JO 2024")
                    .password(passwordEncoder.encode(adminPassword))
                    .build();

            adminRepository.save(admin);
            System.out.printf("Administrateur créé : %s (mot de passe issu de la variable d'environnement)%n", adminEmail);
        } else {
            System.out.printf("Administrateur '%s' déjà existant, aucun ajout effectué.%n", adminEmail);
        }
    }
}
