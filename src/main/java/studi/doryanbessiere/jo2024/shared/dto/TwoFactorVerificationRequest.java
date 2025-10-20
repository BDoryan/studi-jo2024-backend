package studi.doryanbessiere.jo2024.shared.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorVerificationRequest {

    @NotBlank
    private String challengeId;

    @NotBlank
    private String code;
}

