package studi.doryanbessiere.jo2024.common.validation.unique;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {

    public static final String DEFAULT_MESSAGE = "email_already_used";

    String message() default DEFAULT_MESSAGE;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
