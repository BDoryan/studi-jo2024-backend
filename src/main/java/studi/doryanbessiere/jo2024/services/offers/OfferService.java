package studi.doryanbessiere.jo2024.services.offers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;

    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    public Offer getOfferById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
    }

    public Offer createOffer(Offer offer) {
        if (offerRepository.existsByName(offer.getName())) {
            throw new RuntimeException("Une offre avec ce nom existe déjà");
        }
        return offerRepository.save(offer);
    }

    public Offer updateOffer(Long id, Offer updatedOffer) {
        Offer offer = getOfferById(id);
        offer.setName(updatedOffer.getName());
        offer.setDescription(updatedOffer.getDescription());
        offer.setPrice(updatedOffer.getPrice());
        offer.setPersons(updatedOffer.getPersons());
        return offerRepository.save(offer);
    }

    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offre inexistante");
        }
        offerRepository.deleteById(id);
    }
}
