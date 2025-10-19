package studi.doryanbessiere.jo2024.services.offers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.shared.security.AdminOnly;

import java.util.List;

@RestController
@RequestMapping(Routes.Offer.BASE)
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    @PostMapping
    @AdminOnly
    public ResponseEntity<Offer> createOffer(@RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.createOffer(offer));
    }

    @PutMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.updateOffer(id, offer));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
