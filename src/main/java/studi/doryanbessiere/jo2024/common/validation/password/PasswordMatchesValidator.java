package studi.doryanbessiere.jo2024.common.validation.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import studi.doryanbessiere.jo2024.services.customers.dto.RegisterRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        String password = request.getPassword();
        String confirm = request.getConfirmPassword();

        if (password == null || confirm == null) {
            return true;
        }

        boolean match = password.equals(confirm);
        if (!match) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(PasswordMatches.DEFAULT_MESSAGE)
                    .addPropertyNode("confirm_password")
                    .addConstraintViolation();
        }

        return match;
    }
}
