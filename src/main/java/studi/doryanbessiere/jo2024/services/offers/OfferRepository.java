
package studi.doryanbessiere.jo2024.services.offers;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    boolean existsByName(String name);
}
