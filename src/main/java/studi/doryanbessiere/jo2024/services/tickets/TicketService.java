package studi.doryanbessiere.jo2024.services.tickets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import studi.doryanbessiere.jo2024.notifications.EmailNotificationService;
import studi.doryanbessiere.jo2024.notifications.dto.EmailRequest;
import studi.doryanbessiere.jo2024.services.customers.Customer;
import studi.doryanbessiere.jo2024.services.offers.Offer;
import studi.doryanbessiere.jo2024.services.offers.OfferRepository;
import studi.doryanbessiere.jo2024.services.payments.Transaction;
import studi.doryanbessiere.jo2024.services.payments.TransactionRepository;
import studi.doryanbessiere.jo2024.services.tickets.dto.TicketResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TransactionRepository transactionRepository;
    private final OfferRepository offerRepository;
    private final EmailNotificationService emailNotificationService;
    private final Environment environment;

    @Transactional
    public Ticket generateTicketForTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable pour l'identifiant fourni."));

        return ticketRepository.findByTransactionId(transactionId)
                .orElseGet(() -> createTicket(transaction));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForCustomer(Customer customer) {
        return ticketRepository.findAllByCustomerSecretOrderByCreatedAtDesc(customer.getSecretKey())
                .stream()
                .map(ticket -> TicketResponse.builder()
                        .ticketId(ticket.getId())
                        .ticketSecret(ticket.getSecretKey())
                        .status(ticket.getStatus())
                        .entriesAllowed(ticket.getEntriesAllowed())
                        .offerName(ticket.getTransaction().getOfferName())
                        .amount(ticket.getTransaction().getAmount())
                        .transactionStatus(ticket.getTransaction().getStatus().name())
                        .createdAt(ticket.getCreatedAt())
                        .build()
                )
                .toList();
    }

    private Ticket createTicket(Transaction transaction) {
        Offer offer = offerRepository.findById(transaction.getOfferId())
                .orElseThrow(() -> new IllegalStateException(
                        "Offre introuvable pour la transaction " + transaction.getId()));

        int entriesAllowed = Math.max(1, offer.getPersons());

        Ticket ticket = Ticket.builder()
                .secretKey(generateUniqueTicketSecret(transaction.getCustomer().getSecretKey()))
                .customerSecret(transaction.getCustomer().getSecretKey())
                .entriesAllowed(entriesAllowed)
                .status(Ticket.Status.ACTIVE)
                .transaction(transaction)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Generated ticket {} for transaction {}", savedTicket.getId(), transaction.getId());
        sendPaymentConfirmationEmail(transaction);
        return savedTicket;
    }

    private String generateUniqueTicketSecret(String customerSecret) {
        String secret;
        do {
            secret = "TCK-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        } while (secret.equals(customerSecret) || ticketRepository.existsBySecretKey(secret));
        return secret;
    }

    private void sendPaymentConfirmationEmail(Transaction transaction) {
        Customer customer = transaction.getCustomer();
        String frontendUrl = environment.getProperty("APP_FRONTEND_URL", "http://localhost:5173");
        String accountUrl = frontendUrl.endsWith("/") ? frontendUrl + "account" : frontendUrl + "/account";
        String appName = environment.getProperty("APP_NAME", "Billetterie JO 2024");
        String recipientName = StringUtils.hasText(customer.getFirstName()) ? customer.getFirstName() : customer.getEmail();

        Map<String, Object> variables = Map.of(
                "name", recipientName,
                "offerName", transaction.getOfferName(),
                "accountUrl", accountUrl,
                "appName", appName
        );

        emailNotificationService.sendNotification(
                EmailRequest.builder()
                        .to(customer.getEmail())
                        .subject("Confirmation de paiement - " + appName)
                        .templateName("emails/payment-confirmation")
                        .variables(variables)
                        .build()
        );
        log.info("Payment confirmation email sent to {}", customer.getEmail());
    }
}
