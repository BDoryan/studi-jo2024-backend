package studi.doryanbessiere.jo2024.common.validation.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    /**
     * Au moins 8 caractères, une majuscule, une minuscule, un chiffre
     */
    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null ou vide => on laisse @NotBlank s'en charger
        if (value == null || value.isBlank()) {
            return true;
        }

        boolean matches = PASSWORD_POLICY.matcher(value).matches();
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ValidPassword.DEFAULT_MESSAGE)
                    .addConstraintViolation();
        }
        return matches;
    }
}
