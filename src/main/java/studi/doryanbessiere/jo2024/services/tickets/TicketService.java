package studi.doryanbessiere.jo2024.services.tickets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studi.doryanbessiere.jo2024.services.customers.Customer;
import studi.doryanbessiere.jo2024.services.offers.Offer;
import studi.doryanbessiere.jo2024.services.offers.OfferRepository;
import studi.doryanbessiere.jo2024.services.payments.Transaction;
import studi.doryanbessiere.jo2024.services.payments.TransactionRepository;
import studi.doryanbessiere.jo2024.services.tickets.dto.TicketResponse;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TransactionRepository transactionRepository;
    private final OfferRepository offerRepository;

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
        return savedTicket;
    }

    private String generateUniqueTicketSecret(String customerSecret) {
        String secret;
        do {
            secret = "TCK-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        } while (secret.equals(customerSecret) || ticketRepository.existsBySecretKey(secret));
        return secret;
    }
}
