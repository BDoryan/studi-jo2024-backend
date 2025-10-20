package studi.doryanbessiere.jo2024.shared.twofactor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TwoFactorTokenRepository extends JpaRepository<TwoFactorToken, String> {

    void deleteByEmailAndType(String email, TwoFactorTokenType type);
}

