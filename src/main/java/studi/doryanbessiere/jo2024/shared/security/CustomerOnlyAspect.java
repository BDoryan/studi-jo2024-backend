package studi.doryanbessiere.jo2024.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import studi.doryanbessiere.jo2024.common.exceptions.AccessDeniedException;
import studi.doryanbessiere.jo2024.common.exceptions.UnauthorizedException;
import studi.doryanbessiere.jo2024.services.customers.Customer;
import studi.doryanbessiere.jo2024.services.customers.CustomerAuthService;

@Aspect
@Component
@RequiredArgsConstructor
public class CustomerOnlyAspect {

    private final HttpServletRequest request;
    private final CustomerAuthService customerAuthService;

    @Before("@annotation(CustomerOnly)")
    public void verifyCustomerAccess() {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException();
        }

        String token = header.substring(7);
        Customer customer = customerAuthService.getAuthenticatedCustomer(token);
    }
}
