
package studi.doryanbessiere.jo2024.common.validation.unique;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import studi.doryanbessiere.jo2024.customers.CustomerRepository;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final CustomerRepository customerRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Null ou vide → on laisse @NotBlank gérer ça
        if (email == null || email.isBlank()) {
            return true;
        }

        boolean exists = customerRepository.existsByEmail(email);
        if (exists) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(UniqueEmail.DEFAULT_MESSAGE)
                    .addConstraintViolation();
        }

        return !exists;
    }
}
