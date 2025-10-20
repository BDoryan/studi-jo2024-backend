package studi.doryanbessiere.jo2024.common.validation.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    public static final String DEFAULT_MESSAGE = "password_policy_not_respected";

    String message() default DEFAULT_MESSAGE;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
