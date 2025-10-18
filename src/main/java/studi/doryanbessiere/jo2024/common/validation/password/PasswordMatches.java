package studi.doryanbessiere.jo2024.common.validation.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {

    public static final String DEFAULT_MESSAGE = "passwords_do_not_match";

    String message() default DEFAULT_MESSAGE;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
