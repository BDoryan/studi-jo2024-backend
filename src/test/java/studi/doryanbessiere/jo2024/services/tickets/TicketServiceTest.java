package studi.doryanbessiere.jo2024.services.tickets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studi.doryanbessiere.jo2024.services.customers.Customer;
import studi.doryanbessiere.jo2024.services.offers.Offer;
import studi.doryanbessiere.jo2024.services.offers.OfferRepository;
import studi.doryanbessiere.jo2024.services.payments.Transaction;
import studi.doryanbessiere.jo2024.services.payments.TransactionRepository;
import studi.doryanbessiere.jo2024.services.tickets.dto.TicketResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private TicketService ticketService;

    private Transaction transaction;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .secretKey("CUS-SECRET")
                .build();

        transaction = Transaction.builder()
                .id(99L)
                .offerId(12L)
                .offerName("Pack Athlétisme")
                .amount(150.0)
                .customer(customer)
                .status(Transaction.TransactionStatus.PENDING)
                .build();
    }

    @Test
    void generateTicketForTransactionShouldReturnExistingTicket() {
        Ticket existingTicket = Ticket.builder()
                .id(10L)
                .secretKey("TCK-EXISTING")
                .customerSecret(customer.getSecretKey())
                .entriesAllowed(2)
                .status(Ticket.Status.ACTIVE)
                .transaction(transaction)
                .createdAt(OffsetDateTime.now())
                .build();

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(ticketRepository.findByTransactionId(transaction.getId())).thenReturn(Optional.of(existingTicket));

        Ticket result = ticketService.generateTicketForTransaction(transaction.getId());

        assertEquals(existingTicket, result);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @Disabled
    void generateTicketForTransactionShouldCreateTicketWhenMissing() {
        Offer offer = Offer.builder()
                .id(transaction.getOfferId())
                .name("Pack Athlétisme")
                .description("Accès aux finales")
                .price(150.0)
                .persons(3)
                .quantity(50)
                .active(true)
                .build();

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(ticketRepository.findByTransactionId(transaction.getId())).thenReturn(Optional.empty());
        when(offerRepository.findById(transaction.getOfferId())).thenReturn(Optional.of(offer));
        when(ticketRepository.existsBySecretKey(anyString())).thenReturn(false);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(55L);
            ticket.setCreatedAt(OffsetDateTime.now());
            return ticket;
        });

        Ticket created = ticketService.generateTicketForTransaction(transaction.getId());

        assertEquals(55L, created.getId());
        assertTrue(created.getSecretKey().startsWith("TCK-"));
        assertNotEquals(customer.getSecretKey(), created.getSecretKey());
        assertEquals(customer.getSecretKey(), created.getCustomerSecret());
        assertEquals(offer.getPersons(), created.getEntriesAllowed());
        assertEquals(Ticket.Status.ACTIVE, created.getStatus());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void generateTicketForTransactionShouldFailWhenTransactionMissing() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.generateTicketForTransaction(1L));
    }

    @Test
    void generateTicketForTransactionShouldFailWhenOfferMissing() {
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(ticketRepository.findByTransactionId(transaction.getId())).thenReturn(Optional.empty());
        when(offerRepository.findById(transaction.getOfferId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> ticketService.generateTicketForTransaction(transaction.getId()));
    }

    @Test
    void getTicketsForCustomerShouldMapEntitiesToResponses() {
        Transaction paidTransaction = Transaction.builder()
                .id(transaction.getId())
                .offerId(transaction.getOfferId())
                .offerName(transaction.getOfferName())
                .amount(transaction.getAmount())
                .customer(customer)
                .status(Transaction.TransactionStatus.PAID)
                .build();

        Ticket ticket = Ticket.builder()
                .id(7L)
                .secretKey("TCK-ABC")
                .customerSecret(customer.getSecretKey())
                .entriesAllowed(2)
                .status(Ticket.Status.ACTIVE)
                .transaction(paidTransaction)
                .createdAt(OffsetDateTime.of(java.time.LocalDateTime.of(2024, 2, 10, 14, 0), java.time.ZoneOffset.UTC))
                .build();

        when(ticketRepository.findAllByCustomerSecretOrderByCreatedAtDesc(customer.getSecretKey()))
                .thenReturn(List.of(ticket));

        List<TicketResponse> responses = ticketService.getTicketsForCustomer(customer);

        assertEquals(1, responses.size());
        TicketResponse response = responses.get(0);
        assertEquals(ticket.getId(), response.getTicketId());
        assertEquals(ticket.getSecretKey(), response.getTicketSecret());
        assertEquals(ticket.getEntriesAllowed(), response.getEntriesAllowed());
        assertEquals(ticket.getTransaction().getOfferName(), response.getOfferName());
        assertEquals(ticket.getTransaction().getAmount(), response.getAmount());
        assertEquals(ticket.getTransaction().getStatus().name(), response.getTransactionStatus());
        assertEquals(ticket.getCreatedAt(), response.getCreatedAt());
    }
}
