package studi.doryanbessiere.jo2024.services.payments;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import studi.doryanbessiere.jo2024.services.customers.Customer;
import studi.doryanbessiere.jo2024.services.customers.CustomerAuthService;
import studi.doryanbessiere.jo2024.services.offers.Offer;
import studi.doryanbessiere.jo2024.services.offers.OfferRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OfferRepository offerRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerAuthService customerAuthService;

    private final Environment env;

    public Session createCheckoutSession(Long offerId, String authorizationHeader) throws Exception {
        Optional<Offer> offerOpt = offerRepository.findById(offerId);
        if (offerOpt.isEmpty()) {
            throw new IllegalArgumentException("Offre non trouvée.");
        }

        Offer offer = offerOpt.get();

        // Create transaction with PENDING status
        Transaction transaction = transactionRepository.save(
                Transaction.builder()
                        .offerName(offer.getName())
                        .amount(offer.getPrice())
                        .status(Transaction.TransactionStatus.PENDING)
                        .build()
        );

        String frontendUrl = env.getProperty("APP_FRONTEND_URL", "http://localhost:5173");
        String host = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;

        String token = authorizationHeader.substring(7);
        Customer customer = customerAuthService.getAuthenticatedCustomer(token);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(host+"/account/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(host+"/account/cancel")
                .setCustomerEmail(customer.getEmail())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount((long) (offer.getPrice() * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(offer.getName())
                                                                .setDescription(offer.getDescription())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("transaction_id", String.valueOf(transaction.getId()))
                .build();

        Session session = Session.create(params);

        transaction.setStripeSessionId(session.getId());
        transactionRepository.save(transaction);

        return session;
    }
}
