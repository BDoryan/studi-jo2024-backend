package studi.doryanbessiere.jo2024.common.exceptions;

import org.springframework.http.HttpStatus;

public interface ApiException {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
