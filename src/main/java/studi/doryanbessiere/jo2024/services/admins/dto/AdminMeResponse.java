package studi.doryanbessiere.jo2024.services.admins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminMeResponse {

    private String email;
    private String fullName;
    private String role;

}
