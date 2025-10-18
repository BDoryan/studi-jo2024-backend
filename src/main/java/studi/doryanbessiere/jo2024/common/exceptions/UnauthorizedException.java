package studi.doryanbessiere.jo2024.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "unauthorized";

    public UnauthorizedException() {
        super(DEFAULT_MESSAGE);
    }
}
