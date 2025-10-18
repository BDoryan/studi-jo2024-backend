package studi.doryanbessiere.jo2024;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import studi.doryanbessiere.jo2024.common.Routes;
import studi.doryanbessiere.jo2024.common.validation.password.PasswordMatches;
import studi.doryanbessiere.jo2024.common.validation.password.ValidPassword;
import studi.doryanbessiere.jo2024.customers.CustomerAuthController;
import studi.doryanbessiere.jo2024.customers.CustomerAuthService;
import studi.doryanbessiere.jo2024.customers.CustomerRepository;
import studi.doryanbessiere.jo2024.customers.dto.RegisterRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerAuthControllerTest {


    @Autowired
    private CustomerAuthService customerAuthService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        customerRepository.deleteAll();
    }

    // Valid register
    @Test
    void shouldRegisterCustomerSuccessfully() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstname("Jean");
        req.setLastname("Dupont");
        req.setEmail("jean.dupont@example.com");
        req.setPassword("Test1234");
        req.setConfirmPassword("Test1234");

        mockMvc.perform(post(Routes.Auth.Customer.BASE + Routes.Auth.Customer.REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value(CustomerAuthController.CUSTOMER_CREATED));
    }


    @Test
    void shouldCreateCustomerSuccessfully() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstname("Alice");
        req.setLastname("Martin");
        req.setEmail("alice@example.com");
        req.setPassword("Test1234");
        req.setConfirmPassword("Test1234");

        customerAuthService.register(req);

        var customer = customerRepository.findByEmail("alice@example.com").orElseThrow();
        assertEquals("Alice", customer.getFirstName());
        assertNotNull(customer.getSecretKey());
    }

    // Password too weak
    @Test
    void shouldRejectWeakPassword() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstname("Marie");
        req.setLastname("Durand");
        req.setEmail("marie@example.com");
        req.setPassword("abc");
        req.setConfirmPassword("abc");

        mockMvc.perform(post(Routes.Auth.Customer.BASE + Routes.Auth.Customer.REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password")
                        .value(ValidPassword.DEFAULT_MESSAGE));
    }

    // Fields empty
    @Test
    void shouldRejectEmptyFields() throws Exception {
        RegisterRequest req = new RegisterRequest();

        mockMvc.perform(post(Routes.Auth.Customer.BASE + Routes.Auth.Customer.REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstname").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    // Passwords do not match
    @Test
    void shouldRejectNonMatchingPasswords() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstname("Lucas");
        req.setLastname("Martin");
        req.setEmail("lucas@example.com");
        req.setPassword("Test1234");
        req.setConfirmPassword("Different123");

        mockMvc.perform(post(Routes.Auth.Customer.BASE + Routes.Auth.Customer.REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confirm_password")
                        .value(PasswordMatches.DEFAULT_MESSAGE));
    }
}
