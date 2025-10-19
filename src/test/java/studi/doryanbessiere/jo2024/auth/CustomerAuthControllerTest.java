package studi.doryanbessiere.jo2024.auth;

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
import studi.doryanbessiere.jo2024.common.exceptions.InvalidCredentialsException;
import studi.doryanbessiere.jo2024.common.validation.password.PasswordMatches;
import studi.doryanbessiere.jo2024.common.validation.password.ValidPassword;
import studi.doryanbessiere.jo2024.services.customers.CustomerAuthController;
import studi.doryanbessiere.jo2024.services.customers.CustomerAuthService;
import studi.doryanbessiere.jo2024.services.customers.CustomerRepository;
import studi.doryanbessiere.jo2024.services.customers.dto.RegisterRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
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


    }@Test
    void shouldLoginSuccessfully() throws Exception {
        // Create a customer first
        RegisterRequest register = new RegisterRequest();
        register.setFirstname("Paul");
        register.setLastname("Lemoine");
        register.setEmail("paul@example.com");
        register.setPassword("Test1234");
        register.setConfirmPassword("Test1234");
        customerAuthService.register(register);

        // Now attempt to login
        var loginPayload = new java.util.HashMap<String, String>();
        loginPayload.put("email", "paul@example.com");
        loginPayload.put("password", "Test1234");

        mockMvc.perform(post(Routes.Auth.Customer.BASE + Routes.Auth.Customer.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setFirstname("Camille");
        register.setLastname("Bernard");
        register.setEmail("camille@example.com");
        register.setPassword("Correct123");
        register.setConfirmPassword("Correct123");
        customerAuthService.register(register);

        var loginPayload = new java.util.HashMap<String, String>();
        loginPayload.put("email", "camille@example.com");
        loginPayload.put("password", "WrongPassword");

        mockMvc.perform(post(Routes.Auth.Customer.BASE + Routes.Auth.Customer.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(InvalidCredentialsException.DEFAULT_CODE));
    }

    @Test
    void shouldRejectLoginWithUnknownEmail() throws Exception {
        var loginPayload = new java.util.HashMap<String, String>();
        loginPayload.put("email", "unknown@example.com");
        loginPayload.put("password", "SomePass123");

        mockMvc.perform(post(Routes.Auth.Customer.BASE + Routes.Auth.Customer.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(InvalidCredentialsException.DEFAULT_CODE));
    }

    @Test
    void shouldRejectLoginWithEmptyFields() throws Exception {
        // Attempt to login with empty email and password
        var loginPayload = new java.util.HashMap<String, String>();
        loginPayload.put("email", "");
        loginPayload.put("password", "");

        mockMvc.perform(post(Routes.Auth.Customer.BASE + Routes.Auth.Customer.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

}
