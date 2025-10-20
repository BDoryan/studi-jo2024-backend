package studi.doryanbessiere.jo2024.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException implements ApiException {

    public static final String DEFAULT_CODE = "invalid_credentials";

    public InvalidCredentialsException() {
        super(DEFAULT_CODE);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getCode() {
        return DEFAULT_CODE;
    }
}