package studi.doryanbessiere.jo2024.services.customers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import studi.doryanbessiere.jo2024.common.validation.password.PasswordMatches;
import studi.doryanbessiere.jo2024.common.validation.unique.UniqueEmail;
import studi.doryanbessiere.jo2024.common.validation.password.ValidPassword;

@Data
@PasswordMatches
public class RegisterRequest {

    @NotBlank(message = "is_required")
    private String firstname;

    @NotBlank(message = "is_required")
    private String lastname;

    @Email(message = "email_invalid")
    @NotBlank(message = "is_required")
    @UniqueEmail
    private String email;

    @ValidPassword
    @NotBlank(message = "is_required")
    private String password;

    @NotBlank(message = "is_required")
    private String confirmPassword;
}
