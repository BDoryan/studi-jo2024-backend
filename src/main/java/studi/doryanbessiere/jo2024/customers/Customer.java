package studi.doryanbessiere.jo2024.customers;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "secret_key", unique = true, nullable = false)
    private String secretKey;

    @Column(name = "expire_token")
    private String expireToken;

    @Column(name = "expire_token_at")
    private LocalDateTime expireTokenAt;
}
