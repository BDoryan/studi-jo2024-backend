package studi.doryanbessiere.jo2024.services.customers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Email(message = "email_invalid")
    @NotBlank(message = "is_required")
    private String email;

    @NotBlank(message = "is_required")
    private String password;

}
