package studi.doryanbessiere.jo2024.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A simple DTO for API responses containing a status and a message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiMessageResponse {
    private String status;
    private String message;
}
