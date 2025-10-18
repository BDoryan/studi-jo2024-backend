package studi.doryanbessiere.jo2024.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "studi.doryanbessiere.jo2024")
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {

        // Standard API Exception handling
        if (ex instanceof ApiException apiEx) {
            return ResponseEntity
                    .status(apiEx.getStatus())
                    .body(Map.of(
                            "status", "error",
                            "code", apiEx.getCode(),
                            "message", apiEx.getMessage()
                    ));
        }

        // Special handling for validation exceptions
        if (ex instanceof MethodArgumentNotValidException validationEx) {
            Map<String, String> errors = new HashMap<>();

            validationEx.getBindingResult().getAllErrors().forEach(error -> {
                String field = ((FieldError) error).getField();
                String message = error.getDefaultMessage();
                errors.put(field, message);
            });

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "validation_failed");
            response.put("errors", errors);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Manage other exceptions
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof BadRequestException) status = HttpStatus.BAD_REQUEST;
        else if (ex instanceof UnauthorizedException) status = HttpStatus.UNAUTHORIZED;
        else if (ex instanceof ConflictException) status = HttpStatus.CONFLICT;

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(status).body(response);
    }
}
