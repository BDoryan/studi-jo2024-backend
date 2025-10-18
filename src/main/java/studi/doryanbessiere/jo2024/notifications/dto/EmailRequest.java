package studi.doryanbessiere.jo2024.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String to;
    private String subject;
    private String message; // The rendered message content
    private String templateName; // Define the template name to be used
    private Map<String, String> variables; // Parse the variables for template rendering
}
