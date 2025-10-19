package studi.doryanbessiere.jo2024.services.admins;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant un administrateur de la plateforme JO 2024.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "admins")
@Schema(description = "Entité représentant un administrateur enregistré dans le système")
public class Admin {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SCANNER = "SCANNER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identifiant unique de l’administrateur", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Adresse e-mail unique de l’administrateur", example = "admin@jo2024.fr")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Mot de passe chiffré de l’administrateur", example = "$2a$10$...")
    private String password;

    @Schema(description = "Nom complet de l’administrateur", example = "Jean Dupont")
    private String fullName;

    @Builder.Default
    @Schema(description = "Rôle attribué à l’utilisateur", example = "ADMIN")
    private String role = "ADMIN";
}
